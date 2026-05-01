alter table app_user
    add column if not exists first_name varchar(255),
    add column if not exists last_name varchar(255),
    add column if not exists middle_name varchar(255);

update app_user
set
    first_name = coalesce(nullif(first_name, ''), split_part(full_name, ' ', 1)),
    last_name = coalesce(nullif(last_name, ''), nullif(split_part(full_name, ' ', 2), '')),
    middle_name = coalesce(nullif(middle_name, ''), nullif(split_part(full_name, ' ', 3), ''));

update app_user
set
    first_name = coalesce(nullif(first_name, ''), 'Имя'),
    last_name = coalesce(nullif(last_name, ''), 'Фамилия'),
    middle_name = coalesce(nullif(middle_name, ''), 'Отчество');

alter table app_user
    alter column first_name set not null,
    alter column last_name set not null,
    alter column middle_name set not null;
