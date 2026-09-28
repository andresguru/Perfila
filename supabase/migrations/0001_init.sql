-- Perfila: esquema inicial (Supabase / Postgres)
-- Requiere las extensiones postgis (distancias) y vector (match v2).

create extension if not exists postgis;
create extension if not exists vector;

create type user_role as enum ('candidate', 'recruiter');
create type work_mode as enum ('onsite', 'hybrid', 'remote');
create type swipe_decision as enum ('pass', 'like', 'super');

-- Perfil base ligado a auth.users
create table public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  role user_role not null default 'candidate',
  display_name text not null,
  phone text,
  verified boolean not null default false,
  created_at timestamptz not null default now()
);

create table public.companies (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  rfc text unique,
  verified boolean not null default false,
  created_by uuid not null references public.profiles (id),
  created_at timestamptz not null default now()
);

create table public.company_members (
  company_id uuid references public.companies (id) on delete cascade,
  profile_id uuid references public.profiles (id) on delete cascade,
  primary key (company_id, profile_id)
);

create table public.candidate_profiles (
  profile_id uuid primary key references public.profiles (id) on delete cascade,
  headline text,
  years_experience int not null default 0 check (years_experience >= 0),
  skills text[] not null default '{}',
  salary_min int not null check (salary_min >= 0),
  salary_max int not null,
  accepted_modes work_mode[] not null default '{remote,hybrid,onsite}',
  city text,
  location geography(point, 4326),
  max_commute_minutes int not null default 45,
  availability_weeks int not null default 2,
  blind_mode boolean not null default false,
  embedding vector(1536),
  updated_at timestamptz not null default now(),
  check (salary_max >= salary_min)
);

-- El rango salarial es obligatorio en cada vacante.
create table public.jobs (
  id uuid primary key default gen_random_uuid(),
  company_id uuid not null references public.companies (id) on delete cascade,
  title text not null,
  description text,
  salary_min int not null check (salary_min > 0),
  salary_max int not null,
  mode work_mode not null,
  city text,
  location geography(point, 4326),
  required_skills text[] not null default '{}',
  nice_to_have_skills text[] not null default '{}',
  min_years_experience int not null default 0,
  knockout_question text,
  active boolean not null default true,
  embedding vector(1536),
  created_at timestamptz not null default now(),
  check (salary_max >= salary_min)
);

create index jobs_location_idx on public.jobs using gist (location);
create index candidate_location_idx on public.candidate_profiles using gist (location);

-- Cada gesto de un lado sobre el otro.
create table public.swipes (
  id bigint generated always as identity primary key,
  actor_id uuid not null references public.profiles (id) on delete cascade,
  job_id uuid not null references public.jobs (id) on delete cascade,
  candidate_id uuid not null references public.profiles (id) on delete cascade,
  actor_role user_role not null,
  decision swipe_decision not null,
  score int,
  created_at timestamptz not null default now(),
  unique (actor_role, job_id, candidate_id)
);

-- Match = doble sí. Garantía anti-ghosting: la empresa tiene 72 h para escribir.
create table public.matches (
  id uuid primary key default gen_random_uuid(),
  job_id uuid not null references public.jobs (id) on delete cascade,
  candidate_id uuid not null references public.profiles (id) on delete cascade,
  score int not null,
  created_at timestamptz not null default now(),
  respond_by timestamptz not null default now() + interval '72 hours',
  status text not null default 'open' check (status in ('open', 'expired', 'interview', 'offer', 'hired', 'closed')),
  unique (job_id, candidate_id)
);

create table public.messages (
  id bigint generated always as identity primary key,
  match_id uuid not null references public.matches (id) on delete cascade,
  sender_id uuid references public.profiles (id),
  body text not null check (length(body) between 1 and 4000),
  created_at timestamptz not null default now()
);

-- Crea el match cuando ambos lados dicen que sí.
create or replace function public.create_match_on_mutual_like()
returns trigger language plpgsql security definer as $$
begin
  if new.decision in ('like', 'super') and exists (
    select 1 from public.swipes s
    where s.job_id = new.job_id
      and s.candidate_id = new.candidate_id
      and s.actor_role <> new.actor_role
      and s.decision in ('like', 'super')
  ) then
    insert into public.matches (job_id, candidate_id, score)
    values (new.job_id, new.candidate_id, coalesce(new.score, 0))
    on conflict (job_id, candidate_id) do nothing;
  end if;
  return new;
end;
$$;

create trigger swipes_mutual_like
after insert on public.swipes
for each row execute function public.create_match_on_mutual_like();

-- Seguridad a nivel de fila
alter table public.profiles enable row level security;
alter table public.companies enable row level security;
alter table public.company_members enable row level security;
alter table public.candidate_profiles enable row level security;
alter table public.jobs enable row level security;
alter table public.swipes enable row level security;
alter table public.matches enable row level security;
alter table public.messages enable row level security;

create policy "perfil propio" on public.profiles
  for all using (auth.uid() = id) with check (auth.uid() = id);

create policy "vacantes activas visibles" on public.jobs
  for select using (active);

create policy "miembros gestionan vacantes" on public.jobs
  for all using (exists (
    select 1 from public.company_members m
    where m.company_id = jobs.company_id and m.profile_id = auth.uid()
  ));

create policy "candidato gestiona su perfil" on public.candidate_profiles
  for all using (auth.uid() = profile_id) with check (auth.uid() = profile_id);

create policy "swipes propios" on public.swipes
  for insert with check (auth.uid() = actor_id);

create policy "participantes ven el match" on public.matches
  for select using (
    candidate_id = auth.uid() or exists (
      select 1 from public.jobs j
      join public.company_members m on m.company_id = j.company_id
      where j.id = matches.job_id and m.profile_id = auth.uid()
    )
  );

create policy "participantes leen y escriben mensajes" on public.messages
  for all using (exists (
    select 1 from public.matches mt
    where mt.id = messages.match_id and (
      mt.candidate_id = auth.uid() or exists (
        select 1 from public.jobs j
        join public.company_members m on m.company_id = j.company_id
        where j.id = mt.job_id and m.profile_id = auth.uid()
      )
    )
  ));

-- TODO: política para que reclutadores lean perfiles de candidatos compatibles
-- (vía función RPC que aplique filtros duros y modo ciego), y job programado
-- que marque matches como 'expired' cuando pase respond_by sin mensaje de la empresa.
