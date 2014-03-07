DROP TABLE IF EXISTS EVENT_CONFERENCE_ARTICLES_ASSOCIATION;

CREATE TABLE `EVENT_CONFERENCE_ARTICLES_ASSOCIATION` (
  `ID_INTERNAL` int(11) NOT NULL auto_increment,
  `KEY_ROOT_DOMAIN_OBJECT` int(11) NOT NULL default '1',
  `KEY_EVENT_EDITION` int(11) NOT NULL,
  `KEY_PERSON` int(11) NOT NULL,
  `KEY_CONFERENCE_ARTICLE` int(11) NOT NULL,

  PRIMARY KEY  (`ID_INTERNAL`),
  KEY `KEY_ROOT_DOMAIN_OBJECT` (`KEY_ROOT_DOMAIN_OBJECT`),
  KEY `KEY_EVENT_EDITION` (`KEY_EVENT_EDITION`),
  KEY `KEY_PERSON` (`KEY_PERSON`),
  KEY `KEY_CONFERENCE_ARTICLE` (`KEY_CONFERENCE_ARTICLE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;