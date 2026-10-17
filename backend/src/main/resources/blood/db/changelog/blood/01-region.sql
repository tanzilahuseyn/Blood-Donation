--liquibase formatted sql

--changeset ite4120:blood-01-region
--comment Region reference table
/* The list a donation centre's region points at. Natural-key reference data:
   no core.seq_id, no sys columns, like animals.species. */
create table blood.region (
    code                text not null,
    name                text not null,
    constraint region_pk primary key (code)
);
--

--changeset ite4120:blood-01-region-seed
--comment Seed the fifteen Estonian counties
/* Estonia is the assumed jurisdiction until the group's research says
   otherwise (BLOOD.01, Description). Convergent: ON CONFLICT DO NOTHING. */
insert into blood.region (code, name) values
    ('HARJU',      'Harju County'),
    ('HIIU',       'Hiiu County'),
    ('IDA_VIRU',   'Ida-Viru County'),
    ('JOGEVA',     'Jõgeva County'),
    ('JARVA',      'Järva County'),
    ('LAANE',      'Lääne County'),
    ('LAANE_VIRU', 'Lääne-Viru County'),
    ('POLVA',      'Põlva County'),
    ('PARNU',      'Pärnu County'),
    ('RAPLA',      'Rapla County'),
    ('SAARE',      'Saare County'),
    ('TARTU',      'Tartu County'),
    ('VALGA',      'Valga County'),
    ('VILJANDI',   'Viljandi County'),
    ('VORU',       'Võru County')
on conflict do nothing;
--
