ALTER TABLE PROPERTIES DROP CONSTRAINT FK_PROPERTIES_PROPERTYGROUPS_ID
ALTER TABLE LOCATIONPROPERTIES DROP CONSTRAINT FK_LOCATIONPROPERTIES_LOCATION_ID
ALTER TABLE LOCATIONPROPERTIES DROP CONSTRAINT FK_LOCATIONPROPERTIES_PROPERTIES_ID
DROP TABLE ADMIN CASCADE
DROP TABLE LOCATION CASCADE
DROP TABLE PROPERTIES CASCADE
DROP TABLE LOCATIONPROPERTIES CASCADE
DROP TABLE PROPERTYGROUPS CASCADE