-- Add geographic coordinates to villages for the heatmap
alter table village add column lat numeric(9,6);
alter table village add column lng numeric(9,6);

-- Seed approximate coordinates for Andhra Pradesh demo villages
update village set lat = 15.5043, lng = 79.9063 where name = 'Ramapuram';
update village set lat = 16.6167, lng = 80.5000 where name = 'Kondapalli';
update village set lat = 16.2000, lng = 80.2833 where name = 'Gollapalli';

-- Patient photo (stored as base64 data URI for demo portability)
alter table patient add column photo_data text;
