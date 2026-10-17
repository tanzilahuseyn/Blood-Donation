--liquibase formatted sql

--changeset ite4120:blood-schema
--comment Create the blood schema
/* One schema per component, like animals. */
create schema if not exists blood;
--
