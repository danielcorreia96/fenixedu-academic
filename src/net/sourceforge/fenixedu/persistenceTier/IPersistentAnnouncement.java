package net.sourceforge.fenixedu.persistenceTier;

/**
 * @author EP15
 * @author <a href="mailto:joao.mota@ist.utl.pt">Jo�o Mota</a>
 */
import java.util.Date;
import java.util.List;

import net.sourceforge.fenixedu.domain.IAnnouncement;
import net.sourceforge.fenixedu.domain.ISite;

public interface IPersistentAnnouncement extends IPersistentObject {

    public IAnnouncement readAnnouncementByTitleAndCreationDateAndSite(
        String title,
        Date date,
        ISite site)
        throws ExcepcaoPersistencia;
    
    public void delete(IAnnouncement announcement) throws ExcepcaoPersistencia;
    public List readAnnouncementsBySite(ISite site) throws ExcepcaoPersistencia;
    public IAnnouncement readLastAnnouncementForSite(ISite site) throws ExcepcaoPersistencia;
    public List readLastAnnouncementsForSite(ISite site, int n) throws ExcepcaoPersistencia;

}
