drop function if exists update_updated_timestamp;
create function update_updated_timestamp()
returns trigger as $$
begin
    new.updated_timestamp = current_timestamp;
    return new;
end;
$$ language 'plpgsql';

drop trigger if exists update_task_updated_timestamp on task;
drop table if exists task;
create table task(
    id serial not null primary key,
    name text not null,
    description text,
    due_date date,
    autocomplete boolean not null,
    created_timestamp timestamp not null default current_timestamp,
    updated_timestamp timestamp not null default current_timestamp
);
create trigger update_task_updated_timestamp
before update on task
for each row execute procedure update_updated_timestamp();

drop trigger if exists update_task_trigger_updated_timestamp on task_trigger;
drop table if exists task_trigger;
create table task_trigger(
    task_id integer not null primary key references task on delete cascade,
    trigger_type text not null,
    days_between integer,
    day_of_week integer,
    day_of_month integer,
    month_of_year integer,
    created_timestamp timestamp not null default current_timestamp,
    updated_timestamp timestamp not null default current_timestamp
);
create trigger update_task_trigger_updated_timestamp
before update on task_trigger
for each row execute procedure update_updated_timestamp();
