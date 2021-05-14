package wooteco.subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Sections;
import wooteco.subway.domain.SimpleSection;
import wooteco.subway.exception.section.DeleteSectionIsNotPermittedException;
import wooteco.subway.exception.section.NoneOfSectionIncludedInLine;
import wooteco.subway.exception.section.SectionDistanceMismatchException;
import wooteco.subway.exception.section.SectionsAlreadyExistException;

import java.util.List;
import java.util.Optional;

@Service
public class SectionService {

    private final SectionDao sectionDao;

    public SectionService(SectionDao sectionDao) {
        this.sectionDao = sectionDao;
    }

    public void insert(Long lineId, SimpleSection section) {
        sectionDao.insert(new Section(lineId, section));
    }

    public List<Section> findAllByLineId(Long id) {
        return sectionDao.findAllByLineId(id);
    }

    public void validateEndStationsAreIncluded(Long lineId, SimpleSection section) {
        if (sectionDao.isIncludeAllEndStations(new Section(lineId, section))) {
            throw new SectionsAlreadyExistException();
        }
    }

    public int getSectionCountsByLineId(Long lineId) {
        return sectionDao.countsByLineId(lineId);
    }

    @Transactional
    public void insertSections(Long lineId, SimpleSection insertSection) {
        final Optional<Section> optionalSectionConversed =
                sectionDao.findOneIfIncludeConversed(new Section(lineId, insertSection));
        if (optionalSectionConversed.isPresent()) {
            sectionDao.insert(new Section(lineId, insertSection));
            return;
        }

        final Section section = sectionDao.findOneIfInclude(new Section(lineId, insertSection))
                .orElseThrow(NoneOfSectionIncludedInLine::new);
        final SimpleSection updatedSection = section.makeSectionsToStraight(insertSection);
        sectionDao.update(new Section(lineId, updatedSection)); // 기존 섹션을 업데이트함. 삽입된 구간을 포함하여.
        sectionDao.insert(new Section(lineId, insertSection)); // 추가된 섹션을 삽입함.
    }

    public void deleteAllSectionByLineId(Long lineId) {
        sectionDao.deleteAllByLineId(lineId);
    }

    public void deleteSection(Long lineId, Long stationId) {
        validateSectionCount(lineId);
        final List<Section> sections = sectionDao.findAllSectionsIncludeStationId(lineId, stationId);
        if (sections.isEmpty()) {
            throw new DeleteSectionIsNotPermittedException();
        }
        Section section = adjustSection(lineId, new Sections(sections));
        sectionDao.delete(section);
    }

    private void validateSectionCount(Long lineId) {
        final int counts = getSectionCountsByLineId(lineId);
        if (counts <= 1) {
            throw new DeleteSectionIsNotPermittedException();
        }
    }

    private Section adjustSection(Long lineId, Sections sections) {
        if (sections.hasOnlyOneSection()) {
            return sections.section(0);
        }
        sectionDao.update(new Section(lineId, sections.updateSectionToOneLine()));
        return sections.section(1);
    }
}
