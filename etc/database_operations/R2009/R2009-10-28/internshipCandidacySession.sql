alter table `INTERNSHIP_CANDIDACY` add column `OID_INTERNSHIP_CANDIDACY_SESSION` bigint(20);

create table `INTERNSHIP_CANDIDACY_SESSION` (
  `ID_INTERNAL` int(11) NOT NULL auto_increment,
  `CANDIDACY_INTERVAL` longtext,
  `OID` bigint(20),
  `OID_ROOT_DOMAIN_OBJECT` bigint(20),
  primary key (ID_INTERNAL),
  index (OID)
) type=InnoDB, character set latin1 ;

create table `INTERNSHIP_CANDIDACY_SESSION_DESTINATIONS` (
  OID_COUNTRY bigint unsigned default null,
  OID_INTERNSHIP_CANDIDACY_SESSION bigint unsigned default null,
  primary key (OID_COUNTRY, OID_INTERNSHIP_CANDIDACY_SESSION),
  key(OID_COUNTRY, OID_INTERNSHIP_CANDIDACY_SESSION),
  key(OID_COUNTRY),
  key(OID_INTERNSHIP_CANDIDACY_SESSION)
) type=InnoDB;

create table `INTERNSHIP_CANDIDACY_SESSION_UNIVERSITIES` (
  OID_ACADEMICAL_INSTITUTION_UNIT bigint unsigned default null,
  OID_INTERNSHIP_CANDIDACY_SESSION bigint unsigned default null,
  primary key (OID_ACADEMICAL_INSTITUTION_UNIT, OID_INTERNSHIP_CANDIDACY_SESSION),
  key(OID_ACADEMICAL_INSTITUTION_UNIT, OID_INTERNSHIP_CANDIDACY_SESSION),
  key(OID_ACADEMICAL_INSTITUTION_UNIT),
  key(OID_INTERNSHIP_CANDIDACY_SESSION)
) type=InnoDB;