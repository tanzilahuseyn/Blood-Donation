--liquibase formatted sql

--changeset ite4120:blood-90-demo-centres context:demo
--comment Demo donation centres for local development
/* Fictional centres. Operator codes 90000001 and 90000002 are known to the
   imitated business register (MockOperatorRegistryController), so the
   operator lookup shows a hit; 90000099 is unknown to it and shows a miss. */
insert into blood.donation_centre
    (centre_code, name, region_code, address, latitude, longitude, opening_hours, phone, operator_registry_code)
values
    ('DEMO-TLN', 'Tallinn demo donation centre', 'HARJU', 'Näidise 1, Tallinn', 59.437000, 24.753600, 'Mon–Fri 8–16', '+372 600 0001', '90000001'),
    ('DEMO-TRT', 'Tartu demo donation centre',   'TARTU', 'Näidise 2, Tartu',   58.378000, 26.729000, 'Mon–Thu 9–17', '+372 600 0002', '90000002'),
    ('DEMO-PRN', 'Pärnu demo donation centre',   'PARNU', 'Näidise 3, Pärnu',   58.385900, 24.497100, 'Tue, Thu 10–15', null,          '90000099');
--

--changeset ite4120:blood-90-demo-questions context:demo
--comment Placeholder readiness questions
/* PLACEHOLDERS from the group's notes, NOT medical guidance. They stand in
   until the group's research into the official eligibility guidance is done
   (BLOOD.01, Description); the real questions will replace them in a new
   changeset. */
insert into blood.readiness_question
    (question_code, category, question_text, concern_answer, severity, advice, display_order)
values
    ('ATE',        'PREPARATION', 'Have you eaten a proper meal in the last four hours?',      'NO',  'ADVISORY', 'Eat a light, low-fat meal before you go.',                         10),
    ('DRANK',      'PREPARATION', 'Have you drunk plenty of water today?',                     'NO',  'ADVISORY', 'Drink about half a litre of water before you go.',                 20),
    ('SLEPT',      'PREPARATION', 'Did you sleep at least six hours last night?',              'NO',  'ADVISORY', 'Rest first; if you still feel tired, choose another day.',         30),
    ('FEVER',      'ELIGIBILITY', 'Have you had a fever or felt unwell in the last two weeks?', 'YES', 'BLOCKING', 'Wait until you have been well for at least two weeks.',            40),
    ('PIERCING',   'ELIGIBILITY', 'Have you had a tattoo or a piercing in the last four months?', 'YES', 'BLOCKING', 'Wait until four months have passed since the tattoo or piercing.', 50),
    ('AGE_WEIGHT', 'ELIGIBILITY', 'Are you 18 to 65 years old and do you weigh at least 50 kg?', 'NO', 'BLOCKING', 'Ask the blood service whether you can donate before you go.',      60);
--
