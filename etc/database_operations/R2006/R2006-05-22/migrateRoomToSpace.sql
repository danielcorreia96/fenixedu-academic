select concat('insert into SPACE set SPACE.NAME = ''', ROOM.NOME, ''', SPACE.KEY_BUILDING = ', ROOM.KEY_BUILDING, ', SPACE.PISO = ', ROOM.PISO, ', SPACE.TIPO = ', ROOM.TIPO, ', SPACE.CAPACIDADE_NORMAL = ', ROOM.CAPACIDADE_NORMAL, ', SPACE.CAPACIDADE_EXAME = ', CAPACIDADE_EXAME, ', SPACE.ID_INTERNAL = ', ROOM.ID_INTERNAL, ',SPACE.OJB_CONCRETE_CLASS = ''net.sourceforge.fenixedu.domain.space.OldRoom'';') as "" from ROOM;