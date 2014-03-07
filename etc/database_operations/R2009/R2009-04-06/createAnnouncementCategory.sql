DROP TABLE IF EXISTS ANNOUNCEMENT_CATEGORY;
DROP TABLE IF EXISTS ANNOUNCEMENT_CATEGORY_ANNOUNCEMENT;

CREATE TABLE ANNOUNCEMENT_CATEGORY (
	ID_INTERNAL int(11) PRIMARY KEY,
	`TYPE` varchar(100) UNIQUE NOT NULL,
	`KEY_ROOT_DOMAIN_OBJECT` int(11) NOT NULL,
	NAME text NOT NULL) type=InnoDB;

CREATE TABLE ANNOUNCEMENT_CATEGORY_ANNOUNCEMENT (
	KEY_ANNOUNCEMENT int(11),
	KEY_ANNOUNCEMENT_CATEGORY int(11),
	PRIMARY KEY (KEY_ANNOUNCEMENT, KEY_ANNOUNCEMENT_CATEGORY)) type=InnoDB;

ALTER TABLE CONTENT ADD COLUMN KEY_CAMPUS int(11);
ALTER TABLE CONTENT ADD COLUMN PHOTO_URL varchar(255);