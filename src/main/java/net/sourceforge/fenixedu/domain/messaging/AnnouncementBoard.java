package net.sourceforge.fenixedu.domain.messaging;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import net.sourceforge.fenixedu.domain.FileContent;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.Site;
import net.sourceforge.fenixedu.domain.accessControl.Group;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.injectionCode.AccessControl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;

import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import com.google.common.collect.Ordering;

public abstract class AnnouncementBoard extends AnnouncementBoard_Base {

    public static class AnnouncementPresentationBean extends TreeSet<Announcement> {

        private int maxElements = 0;

        private final Comparator<Announcement> comparator;

        public AnnouncementPresentationBean(final int maxElements, final Comparator<Announcement> comparator) {
            super(comparator);
            this.maxElements = maxElements;
            this.comparator = comparator;
        }

        @Override
        public boolean add(Announcement announcement) {
            if (size() < maxElements) {
                return super.add(announcement);
            }
            final Announcement oldestAnnouncement = last();
            if (comparator.compare(announcement, oldestAnnouncement) < 0) {
                remove(oldestAnnouncement);
                return super.add(announcement);
            }
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Announcement> c) {
            for (final Announcement announcement : c) {
                add(announcement);
            }
            return true;
        }

    }

    public static final Comparator<AnnouncementBoard> NEWEST_FIRST = new Comparator<AnnouncementBoard>() {
        @Override
        public int compare(AnnouncementBoard o1, AnnouncementBoard o2) {
            int result = -o1.getCreationDate().compareTo(o2.getCreationDate());
            return (result == 0) ? o1.getExternalId().compareTo(o2.getExternalId()) : result;
        }
    };

    public static final Comparator<AnnouncementBoard> BY_NAME = new Comparator<AnnouncementBoard>() {
        @Override
        public int compare(AnnouncementBoard o1, AnnouncementBoard o2) {
            int result = Collator.getInstance().compare(o1.getName().getContent(), o2.getName().getContent());
            return (result == 0) ? o1.getExternalId().compareTo(o2.getExternalId()) : result;
        }
    };

    public static class AcessLevelPredicate implements Predicate {
        AnnouncementBoardAccessLevel level;
        Person person;

        public AcessLevelPredicate(AnnouncementBoardAccessLevel level, Person person) {
            this.level = level;
            this.person = person;
        }

        @Override
        public boolean evaluate(Object arg0) {
            final AnnouncementBoard board = (AnnouncementBoard) arg0;
            boolean result = false;
            switch (level) {
            case ABAL_ALL:
                if (board.getManagers() == null || board.getWriters() == null || board.getReaders() == null
                        || board.getManagers().isMember(person) || board.getWriters().isMember(person)
                        || board.getReaders().isMember(person)) {
                    result = true;
                }
                break;
            case ABAL_MANAGE:
                if (board.getManagers() == null || board.getManagers().isMember(person)) {
                    result = true;
                }
                break;
            case ABAL_READ:
                if (board.getReaders() == null || board.getReaders().isMember(person)) {
                    result = true;
                }
                break;
            case ABAL_WRITE:
                if (board.getWriters() == null || board.getWriters().isMember(person)) {
                    result = true;
                }
                break;
            }

            return result;
        }
    }

    public static class AcessTypePredicate implements Predicate {
        AnnouncementBoardAccessType type;
        Person person;

        public AcessTypePredicate(AnnouncementBoardAccessType type, Person person) {
            this.type = type;
            this.person = person;
        }

        @Override
        public boolean evaluate(Object arg0) {
            final AnnouncementBoard board = (AnnouncementBoard) arg0;
            switch (type) {
            case ABAT_PUBLIC:
                return board.isPublicToRead();

            case ABAT_PRIVATE:
                return (board.getReaders() != null);

            case ABAT_ALL:
                return true;

            default:
                return false;
            }
        }
    }

    public AnnouncementBoard() {
        super();
        setBennu(Bennu.getInstance());
        setCreationDate(new DateTime());
    }

    public List<Announcement> getActiveAnnouncements() {
        final List<Announcement> activeAnnouncements = new ArrayList<Announcement>();

        for (Announcement announcement : getAnnouncementSet()) {
            if (announcement.isActive() && announcement.getVisible() && announcement.getApproved()) {
                activeAnnouncements.add(announcement);
            }
        }

        return activeAnnouncements;
    }

    public List<Announcement> getApprovedAnnouncements() {
        final Person person = AccessControl.getPerson();
        final List<Announcement> activeAnnouncements = new ArrayList<Announcement>();
        for (Announcement announcement : getAnnouncementSet()) {
            if (announcement.isActive()
                    && announcement.getVisible()
                    && (announcement.getApproved() || (person != null && (announcement.getCreator().equals(person) || announcement
                            .getAnnouncementBoard().getApprovers().isMember(person))))) {
                activeAnnouncements.add(announcement);
            }
        }

        return activeAnnouncements;

    }

    public int getActiveAnnoucementsCount() {
        return getActiveAnnouncements().size();
    }

    public Collection<Announcement> getVisibleAnnouncements() {
        final Collection<Announcement> activeAnnouncements = new ArrayList<Announcement>();
        for (Announcement announcement : getAnnouncementSet()) {
            if (announcement.getVisible()) {
                activeAnnouncements.add(announcement);
            }
        }
        return activeAnnouncements;
    }

    public void addVisibleAnnouncements(final AnnouncementPresentationBean announcementPresentationBean) {
        for (final Announcement announcement : getAnnouncementSet()) {
            if (announcement.getVisible() && announcement.isInPublicationPeriod()) {
                announcementPresentationBean.add(announcement);
            }
        }
    }

    public boolean isDeletable() {
        return getAnnouncementSet().isEmpty();
    }

    public void delete() {
        if (!isDeletable()) {
            throw new DomainException("cannot.delete.non.empty.announcement.board");
        }
        removeBookmarkedBoards();
        deleteDomainObject();
    }

    private void removeBookmarkedBoards() {
        for (final Person person : this.getBookmarkOwner()) {
            removeBookmarkOwner(person);
        }
    }

    public boolean isPublicToRead() {
        return getReaders() == null;
    }

    public boolean isPublicToWrite() {
        return getWriters() == null;
    }

    public boolean isPublicToManage() {
        return getManagers() == null;
    }

    public boolean isPublicToApprove() {
        return getApprovers() == null;
    }

    public boolean hasReader(Person person) {
        return (isPublicToRead() || getReaders().isMember(person));
    }

    public boolean isCurrentUserReader() {
        return hasReader(AccessControl.getPerson());
    }

    public boolean hasWriter(Person person) {
        return (isPublicToWrite() || getWriters().isMember(person));
    }

    public boolean isCurrentUserWriter() {
        return hasWriter(AccessControl.getPerson());
    }

    public boolean isCurrentUserApprover() {
        return hasApprover(AccessControl.getPerson());
    }

    public boolean hasApprover(Person person) {
        return (isPublicToApprove() || getApprovers().isMember(person));
    }

    public boolean hasManager(Person person) {
        return (isPublicToManage() || getManagers().isMember(person));
    }

    public boolean isCurrentUserManager() {
        return hasManager(AccessControl.getPerson());
    }

    public boolean hasReaderOrWriter(Person person) {
        return hasReader(person) || hasWriter(person);
    }

    public boolean isCurrentUserReaderOrWriter() {
        return hasReaderOrWriter(AccessControl.getPerson());
    }

    abstract public String getFullName();

    abstract public String getQualifiedName();

    protected boolean isGroupMember(final Group group) {
        return group == null || group.isMember(AccessControl.getPerson());
    }

    public boolean isReader() {
        return isGroupMember(getReaders());
    }

    public boolean isWriter() {
        return isGroupMember(getWriters());
    }

    public boolean isManager() {
        return isGroupMember(getManagers());
    }

    public boolean isApprover() {
        return isGroupMember(getApprovers());
    }

    public boolean isBookmarkOwner() {
        return AccessControl.getPerson().getBookmarkedBoardsSet().contains(this);
    }

    private List<Announcement> filterAnnouncements(Collection<Announcement> announcements, final Predicate predicate) {
        return (List<Announcement>) CollectionUtils.select(announcements, new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                Announcement announcement = (Announcement) arg0;
                return announcement.getApproved() && predicate.evaluate(arg0);
            }

        });
    }

    public List<Announcement> getActiveAnnouncementsFor(final YearMonthDay date) {
        return filterAnnouncements(getAnnouncementSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                Announcement announcement = (Announcement) arg0;
                DateTime announcementDate = announcement.getReferedSubjectBegin();
                DateTime finalAnnouncementDate = announcement.getReferedSubjectEnd();
                return announcement.isActive()
                        && announcement.getVisible()
                        && announcementDate != null
                        && (announcementDate.toYearMonthDay().equals(date) || (announcementDate.toYearMonthDay().isBefore(date)
                                && finalAnnouncementDate != null && finalAnnouncementDate.toYearMonthDay().isAfter(date)));
            }
        });
    }

    public List<Announcement> getActiveAnnouncementsUntil(final YearMonthDay date) {
        return filterAnnouncements(getAnnouncementSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                Announcement announcement = (Announcement) arg0;
                DateTime announcementDate = announcement.getReferedSubjectBegin();
                return announcement.isActive() && announcement.getVisible() && announcementDate != null
                        && announcementDate.toYearMonthDay().isBefore(date);
            }
        });
    }

    public List<Announcement> getActiveAnnouncementsAfter(final YearMonthDay date) {
        return filterAnnouncements(getAnnouncementSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                Announcement announcement = (Announcement) arg0;
                DateTime announcementDate = announcement.getReferedSubjectBegin();
                return announcement.isActive() && announcement.getVisible() && announcementDate != null
                        && announcementDate.toYearMonthDay().isAfter(date);
            }
        });
    }

    public List<Announcement> getActiveAnnouncementsBefore(final YearMonthDay date) {
        return filterAnnouncements(getAnnouncementSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                Announcement announcement = (Announcement) arg0;
                DateTime announcementDate = announcement.getReferedSubjectBegin();
                DateTime endDate = announcement.getReferedSubjectEnd();
                return announcement.isActive() && announcement.getVisible() && announcementDate != null
                        && announcementDate.toYearMonthDay().isBefore(date)
                        && (endDate == null || endDate.toYearMonthDay().isBefore(date));
            }
        });
    }

    @Override
    public void addFileContent(FileContent fileContent) {
        logAddFile(fileContent);
        super.addFileContent(fileContent);
    }

    public List<FileContent> getFilesSortedByDate() {
        List<FileContent> files = new ArrayList<>(getFileContentSet());
        Collections.sort(files, Ordering.from(new Comparator<FileContent>() {
            @Override
            public int compare(FileContent f1, FileContent f2) {
                return f1.getUploadTime().compareTo(f2.getUploadTime());
            }

        }).reverse());

        return files;
    }

    public String getKeywords() {
        StringBuilder keywords = new StringBuilder();
        for (Announcement announcement : getAnnouncementSet()) {
            MultiLanguageString announcementKeyword = announcement.getKeywords();
            if (announcementKeyword != null) {
                if (keywords.length() > 0) {
                    keywords.append(", ");
                }
                keywords.append(announcementKeyword.getContent());
            }
        }
        return keywords.toString();
    }

    public Boolean getInitialAnnouncementsApprovedState() {
        return false;
    }

    public String getSiteParamForAnnouncementBoard(Announcement announcement) {

        StringBuffer actionPath = new StringBuffer();
        actionPath.append("&announcementId=");
        actionPath.append(announcement.getExternalId());

        return actionPath.toString();
    }

    @Atomic
    public FileContent addFileToBoard(String fileName, byte[] content, String creatorName) {
        return new PublicBoardFileContent(fileName, content, creatorName, this);
    }

    public void logCreate(Announcement announcement) {
    }

    public void logEdit(Announcement announcement) {
    }

    public void logRemove(Announcement announcement) {
    }

    public void logAddFile(FileContent attachment) {
    }

    public abstract Site getSite();

    @Deprecated
    public java.util.Set<net.sourceforge.fenixedu.domain.Person> getBookmarkOwner() {
        return getBookmarkOwnerSet();
    }

    @Deprecated
    public boolean hasAnyBookmarkOwner() {
        return !getBookmarkOwnerSet().isEmpty();
    }

    @Deprecated
    public boolean hasWriters() {
        return getWriters() != null;
    }

    @Deprecated
    public boolean hasManagers() {
        return getManagers() != null;
    }

    @Deprecated
    public boolean hasApprovers() {
        return getApprovers() != null;
    }

    @Deprecated
    public boolean hasReaders() {
        return getReaders() != null;
    }

    @Deprecated
    public boolean hasMandatory() {
        return getMandatory() != null;
    }

}
