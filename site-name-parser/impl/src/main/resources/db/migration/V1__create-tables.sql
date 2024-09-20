create table if not exists task(
    id uuid primary key,
    status varchar(50) not null,
    created_at timestamp not null,
    task_data varchar not null,
    finished_at timestamp
);

create table if not exists title(
    id uuid primary key,
    url varchar not null,
    title_value varchar not null,
    created_at timestamp not null,
    task_id uuid references task(id)
);