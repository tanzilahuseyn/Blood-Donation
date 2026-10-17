--liquibase formatted sql

--changeset ite4120:blood-02-donation-centre
--comment The donation centre directory
/* The rules of BLOOD.01, Data model, held by the database as well as the
   service, so no other path can store nonsense:
   - centre_code unique among ACTIVE rows only (the partial index below);
   - a location is a real coordinate, and latitude and longitude come
     together or not at all;
   - operator_registry_code is TEXT of exactly eight digits, an identifier,
     never a number. The operator's name and address are not stored: the
     business register owns them. */
create table blood.donation_centre (
    id                  bigint default nextval('core.seq_id') not null,
    centre_code         text not null,
    name                text not null,
    region_code         text not null,
    address             text not null,
    latitude            numeric(9,6),
    longitude           numeric(9,6),
    opening_hours       text,
    phone               text,
    operator_registry_code text,
    sys_status          char(1),
    sys_version         int,
    sys_created_at      timestamptz,
    sys_created_by      text,
    sys_modified_at     timestamptz,
    sys_modified_by     text,
    constraint donation_centre_pk primary key (id),
    constraint donation_centre_region_fk foreign key (region_code) references blood.region (code),
    constraint donation_centre_latitude_chk check (latitude between -90 and 90),
    constraint donation_centre_longitude_chk check (longitude between -180 and 180),
    constraint donation_centre_location_chk check ((latitude is null) = (longitude is null)),
    constraint donation_centre_operator_chk check (operator_registry_code ~ '^[0-9]{8}$')
);

select core.create_table_metadata('blood.donation_centre');

/* Unique among ACTIVE rows only: a retired centre's code may be reused. */
create unique index donation_centre_code_ukey
    on blood.donation_centre (centre_code) where (sys_status = 'A');

create index donation_centre_region_idx on blood.donation_centre (region_code) where (sys_status = 'A');
--
