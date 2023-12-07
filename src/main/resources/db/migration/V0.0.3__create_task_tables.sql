drop trigger if exists update_task_completion_updated_timestamp on task_completion;
drop table if exists task_completion;
create table task_completion(
    id serial not null primary key,
    task_id integer not null references task,
    completion_timestamp timestamp not null,
    due_date_when_completed date not null,
    was_autocomplete boolean not null,
    created_timestamp timestamp not null default current_timestamp,
    updated_timestamp timestamp not null default current_timestamp
);
create trigger update_task_completion_updated_timestamp
before update on task_completion
for each row execute procedure update_updated_timestamp();