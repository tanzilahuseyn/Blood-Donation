--liquibase formatted sql

--changeset ite4120:blood-03-readiness-question
--comment The readiness questionnaire's questions and advice
/* Content, not answers. The questions arrive by changeset and are served by
   the API; a person's answers are evaluated in the browser and are never
   stored. There is deliberately no answers table (BLOOD.01, privacy
   constraint). A concern is raised when the answer equals concern_answer;
   severity says whether it can be fixed before going or means not today. */
create table blood.readiness_question (
    id                  bigint default nextval('core.seq_id') not null,
    question_code       text not null,
    category            text not null,
    question_text       text not null,
    concern_answer      text not null,
    severity            text not null,
    advice              text not null,
    display_order       int not null,
    sys_status          char(1),
    sys_version         int,
    sys_created_at      timestamptz,
    sys_created_by      text,
    sys_modified_at     timestamptz,
    sys_modified_by     text,
    constraint readiness_question_pk primary key (id),
    constraint readiness_question_category_chk check (category in ('PREPARATION', 'ELIGIBILITY')),
    constraint readiness_question_concern_chk check (concern_answer in ('YES', 'NO')),
    constraint readiness_question_severity_chk check (severity in ('ADVISORY', 'BLOCKING')),
    constraint readiness_question_order_chk check (display_order >= 0)
);

select core.create_table_metadata('blood.readiness_question');

create unique index readiness_question_code_ukey
    on blood.readiness_question (question_code) where (sys_status = 'A');

create index readiness_question_order_idx
    on blood.readiness_question (display_order) where (sys_status = 'A');
--
