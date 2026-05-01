create table app_user (
    id bigserial primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    full_name varchar(255) not null,
    role varchar(32) not null,
    active boolean not null default true,
    avatar_path varchar(512)
);

create table student_group (
    id bigserial primary key,
    name varchar(100) not null unique,
    curator_id bigint references app_user(id)
);

create table group_member (
    id bigserial primary key,
    group_id bigint not null references student_group(id) on delete cascade,
    student_id bigint not null unique references app_user(id) on delete cascade
);

create table dish (
    id bigserial primary key,
    name varchar(255) not null,
    description text,
    proteins_per_100g numeric(6,2),
    fats_per_100g numeric(6,2),
    carbs_per_100g numeric(6,2),
    calories_per_100g numeric(6,2),
    photo_path varchar(512),
    created_by_chef_id bigint references app_user(id)
);

create table menu_item (
    id bigserial primary key,
    menu_date date not null,
    meal_slot varchar(32) not null,
    dish_id bigint not null references dish(id) on delete cascade
);

create table meal_voucher (
    id bigserial primary key,
    student_id bigint not null references app_user(id) on delete cascade,
    issue_date date not null,
    meal_slot varchar(32) not null,
    status varchar(32) not null,
    issued_by_curator_id bigint not null references app_user(id),
    redeemed_by_chef_id bigint references app_user(id),
    redeemed_at timestamp
);

create table feedback (
    id bigserial primary key,
    student_id bigint not null references app_user(id),
    message text not null,
    created_at timestamp not null default now()
);

create unique index uq_voucher_student_date_slot on meal_voucher(student_id, issue_date, meal_slot);
create index idx_menu_item_date_slot on menu_item(menu_date, meal_slot);
