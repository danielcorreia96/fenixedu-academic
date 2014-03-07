CREATE TABLE PENALTY_EXEMPTION (
  ID_INTERNAL int(11) unsigned NOT NULL auto_increment,
  OJB_CONCRETE_CLASS VARCHAR(255) NOT NULL,
  EXEMPTION_TYPE VARCHAR(255) NOT NULL,
  COMMENTS TEXT NULL,
  WHEN_CREATED DATETIME NOT NULL,
  KEY_ROOT_DOMAIN_OBJECT INT(11) NOT NULL default '1',
  KEY_GRATUITY_EVENT INT(11) NOT NULL,
  KEY_EMPLOYEE INT(11) NOT NULL,
  KEY_INSTALLMENT INT(11) NULL,
  PRIMARY KEY  (ID_INTERNAL),
  KEY `KEY_GRATUITY_EVENT` (KEY_GRATUITY_EVENT),
  KEY `KEY_EMPLOYEE` (KEY_EMPLOYEE),
  KEY `KEY_INSTALLMENT`  (KEY_INSTALLMENT),
  KEY `KEY_ROOT_DOMAIN_OBJECT`  (KEY_ROOT_DOMAIN_OBJECT)
) ENGINE=InnoDB;