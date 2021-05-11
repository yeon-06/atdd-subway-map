package wooteco.subway.line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import wooteco.subway.exception.illegal.BothStationInLineException;
import wooteco.subway.exception.illegal.IllegalInputException;
import wooteco.subway.exception.nosuch.BothStationNotInLineException;
import wooteco.subway.station.Station;

public class StationsInLine {
    private final List<Station> stations;
    private final List<Long> stationIds;

    public StationsInLine(List<Station> stations) {
        this.stations = stations;
        this.stationIds = stations.stream()
            .map(Station::getId)
            .collect(Collectors.toList());
    }

    public static StationsInLine from(Map<Station, Station> sections) {
        List<Station> stations = new ArrayList<>();
        Station startStation = sections.keySet().stream()
            .filter(station -> !sections.containsValue(station))
            .findAny()
            .orElseThrow(IllegalInputException::new);
        stations.add(startStation);

        for (int i = 0; i < sections.size(); i++) {
            Station endStation = sections.get(startStation);
            stations.add(endStation);
            startStation = endStation;
        }

        return new StationsInLine(stations);
    }

    public void validStations(long upStationId, long downStationId) {
        if (stationIds.containsAll(Arrays.asList(upStationId, downStationId))) {
            throw new BothStationInLineException();
        }

        if (!stationIds.contains(upStationId) && !stationIds.contains(downStationId)) {
            throw new BothStationNotInLineException();
        }
    }

    public boolean isEndStations(long upStationId, long downStationId) {
        return stationIds.get(0) == downStationId || stationIds.get(stations.size() - 1) == upStationId;
    }

    public boolean contains(long stationId) {
        return stationIds.contains(stationId);
    }

    public List<Station> getStations() {
        return stations;
    }
}
