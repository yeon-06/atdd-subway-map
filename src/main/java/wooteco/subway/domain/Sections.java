package wooteco.subway.domain;

import java.util.List;
import java.util.Optional;

public class Sections {
    private final List<Section> value;

    public Sections(List<Section> value) {
        this.value = value;
    }

    /**
     * 새로운 구간을 등록하는 메서드
     *
     * @param newSection 추가하고자 하는 구간
     * @return 데이터가 변경된 Section의 List
     */
    public List<Section> add(Section newSection) {
        for (Section section : value) {
            validSection(newSection, section);

            if (newSection.canConnectWithUpStation(section)) {
                Optional<Section> foundSection = findByUpStationId(newSection.getUpStationId());
                if (foundSection.isPresent()) {
                    foundSection.get().updateUpStationId(newSection.getDownStationId());
                    foundSection.get().shortenDistance(newSection.getDistance());
                    return List.of(newSection, foundSection.get());
                }
                break;
            }

            if (newSection.canConnectWithDownStation(section)) {
                Optional<Section> foundSection = findByDownStationId(newSection.getDownStationId());
                if (foundSection.isPresent()) {
                    foundSection.get().updateDownStationId(newSection.getUpStationId());
                    foundSection.get().shortenDistance(newSection.getDistance());
                    return List.of(newSection, foundSection.get());
                }
                break;
            }
        }
        return List.of(newSection);
    }

    private void validSection(Section newSection, Section section) {
        if (newSection.isSameDownStationId(section) && newSection.isSameUpStationId(section)) {
            throw new IllegalArgumentException("해당 구간은 이미 등록되어 있습니다.");
        }
    }

    private Optional<Section> findByUpStationId(Long id) {
        return value.stream()
                .filter(other -> other.getUpStationId().equals(id))
                .findFirst();
    }

    private Optional<Section> findByDownStationId(Long id) {
        return value.stream()
                .filter(other -> other.getDownStationId().equals(id))
                .findFirst();
    }

    public List<Section> getValue() {
        return value;
    }
}
