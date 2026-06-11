package org.example.apiInteraction;

import org.example.apiInteraction.RunArgs.FileType;
import org.example.apiInteraction.RunArgs.UserInteractionType;
import org.example.apiInteraction.apiHandling.ApiRecord;
import org.example.apiInteraction.cliInteraction.InteractiveUtils;
import org.example.apiInteraction.resultFormatting.CSVResultFormatter;
import org.example.apiInteraction.resultFormatting.CustomFormatter;
import org.example.apiInteraction.resultFormatting.JSONResultFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInteractionTest {

    private ApiRecord apiNoPath;
    private ApiRecord apiWithPath;
    private ApiRecord apiWithPathNoOptions;

    @Mock
    private InteractiveUtils mockUtils;

    private UserInteraction ui;

    @BeforeEach
    void setUp() {
        apiNoPath = new ApiRecord();
        apiNoPath.setId(0); apiNoPath.setName("Simple");
        apiNoPath.setAdditionalPathNeeded(false);

        apiWithPath = new ApiRecord();
        apiWithPath.setId(1); apiWithPath.setName("Movies");
        apiWithPath.setAdditionalPathNeeded(true);
        apiWithPath.setAdditionalPaths(new String[]{"popular", "top_rated"});

        apiWithPathNoOptions = new ApiRecord();
        apiWithPathNoOptions.setId(2); apiWithPathNoOptions.setName("Broken");
        apiWithPathNoOptions.setAdditionalPathNeeded(true);
        apiWithPathNoOptions.setAdditionalPaths(new String[]{});

        ApiRecord[] apis = {apiNoPath, apiWithPath, apiWithPathNoOptions};
        ui = new UserInteraction(apis, mockUtils);
    }

    // resolveApis

    @Test
    void resolveApis_singleId_returnsCorrectApi() {
        Set<ApiRecord> result = ui.resolveApis(new int[]{0});
        assertEquals(1, result.size());
        assertTrue(result.contains(apiNoPath));
    }

    @Test
    void resolveApis_multipleIds_returnsAll() {
        Set<ApiRecord> result = ui.resolveApis(new int[]{0, 1});
        assertEquals(2, result.size());
        assertTrue(result.contains(apiNoPath));
        assertTrue(result.contains(apiWithPath));
    }

    @Test
    void resolveApis_unknownId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> ui.resolveApis(new int[]{99}));
    }

    @Test
    void resolveApis_emptyArray_returnsEmptySet() {
        Set<ApiRecord> result = ui.resolveApis(new int[]{});
        assertTrue(result.isEmpty());
    }
    
    // buildFormatter

    @Test
    void buildFormatter_json_returnsJsonFormatter() {
        CustomFormatter f = ui.buildFormatter(FileType.JSON);
        assertInstanceOf(JSONResultFormatter.class, f);
    }

    @Test
    void buildFormatter_csv_returnsCsvFormatter() {
        CustomFormatter f = ui.buildFormatter(FileType.CSV);
        assertInstanceOf(CSVResultFormatter.class, f);
    }

    // resolveAdditionalPaths

    @Test
    void resolveAdditionalPaths_noPathNeeded_returnsEmptyMap() {
        Set<ApiRecord> apis = Set.of(apiNoPath);
        Map<Integer, String> result =
                ui.resolveAdditionalPaths(apis, UserInteractionType.AUTOMATIC);
        assertTrue(result.isEmpty());
    }

    @Test
    void resolveAdditionalPaths_automaticMode_picksFromAvailableOptions() {
        Set<ApiRecord> apis = Set.of(apiWithPath);
        Map<Integer, String> result =
                ui.resolveAdditionalPaths(apis, UserInteractionType.AUTOMATIC);

        assertEquals(1, result.size());
        String chosen = result.get(1);
        assertTrue(chosen.equals("popular") || chosen.equals("top_rated"),
                "Unexpected path: " + chosen);
    }

    @Test
    void resolveAdditionalPaths_interactiveMode_delegatesToInteractiveUtils() {
        when(mockUtils.askUserForAdditionalPath(apiWithPath)).thenReturn("top_rated");

        Set<ApiRecord> apis = Set.of(apiWithPath);
        Map<Integer, String> result =
                ui.resolveAdditionalPaths(apis, UserInteractionType.INTERACTIVE);

        assertEquals("top_rated", result.get(1));
        verify(mockUtils).askUserForAdditionalPath(apiWithPath);
    }

    @Test
    void resolveAdditionalPaths_emptyOptions_throwsIllegalArgument() {
        Set<ApiRecord> apis = Set.of(apiWithPathNoOptions);
        assertThrows(IllegalArgumentException.class,
                () -> ui.resolveAdditionalPaths(apis, UserInteractionType.AUTOMATIC));
    }

    @Test
    void resolveAdditionalPaths_mixedApis_onlyPathNeededOnesResolved() {
        when(mockUtils.askUserForAdditionalPath(apiWithPath)).thenReturn("popular");

        Set<ApiRecord> apis = Set.of(apiNoPath, apiWithPath);
        Map<Integer, String> result =
                ui.resolveAdditionalPaths(apis, UserInteractionType.INTERACTIVE);

        assertFalse(result.containsKey(0), "apiNoPath should not be in result");
        assertTrue(result.containsKey(1));
    }
}
