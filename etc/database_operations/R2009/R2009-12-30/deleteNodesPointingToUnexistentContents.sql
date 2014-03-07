CREATE TEMPORARY TABLE XPTO AS select NODE.* from NODE left join CONTENT on NODE.OID_CHILD = CONTENT.OID where CONTENT.OID is null;
delete from NODE where ID_INTERNAL in (select XPTO.ID_INTERNAL FROM XPTO);
drop TEMPORARY TABLE XPTO;