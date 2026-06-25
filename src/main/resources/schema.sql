create table if not exists announcement (
                                            id bigserial primary key,
                                            company_name varchar(80) not null,
    title varchar(120) not null,
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null
    );

create index if not exists idx_announcement_company_name on announcement (company_name);
create index if not exists idx_announcement_status on announcement (status);

create table if not exists status_report (
                                             id bigserial primary key,
                                             member_id bigint not null,
                                             announcement_id bigint not null references announcement (id),
    stage varchar(30) not null,
    notified_date date not null,
    notified_time_text varchar(20) not null,
    notification_channel varchar(20) not null,
    result varchar(20) not null,
    created_at timestamp not null,
    updated_at timestamp not null
    );

create index if not exists idx_status_report_announcement_created on status_report (announcement_id, created_at);
create index if not exists idx_status_report_member_announcement on status_report (member_id, announcement_id);

create table if not exists realtime_event (
                                              id bigserial primary key,
                                              announcement_id bigint not null references announcement (id),
    event_type varchar(40) not null,
    stage varchar(30) not null,
    result varchar(20) not null,
    title varchar(120) not null,
    body varchar(160) not null,
    created_at timestamp not null,
    updated_at timestamp not null
    );

create index if not exists idx_realtime_event_announcement_id on realtime_event (announcement_id);
create index if not exists idx_realtime_event_created_id on realtime_event (created_at, id);