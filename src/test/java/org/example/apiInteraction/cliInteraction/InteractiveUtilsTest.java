package org.example.apiInteraction.cliInteraction;

import org.example.apiInteraction.RunArgs;
import org.example.apiInteraction.apiHandling.ApiRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class InteractiveUtilsTest {

    private static final InputStream ORIGINAL_IN = System.in;

    private ApiRecord[] makeApis() {
        ApiRecord a = new ApiRecord();
        a.setId(0); a.setName("Addresses");
        a.setKeyNeeded(false); a.setBaseRequestURL("https://fakerapi.it/");
        a.setAdditionalPathNeeded(false);

        ApiRecord b = new ApiRecord();
        b.setId(1); b.setName("Movies");
        b.setKeyNeeded(true); b.setBaseRequestURL("https://movies.com/");
        b.setAdditionalPathNeeded(true);
        b.setAdditionalPaths(new String[]{"popular", "top_rated"});

        ApiRecord c = new ApiRecord();
        c.setId(2); c.setName("Exchange");
        c.setKeyNeeded(false); c.setBaseRequestURL("https://exchange.com/");
        c.setAdditionalPathNeeded(true);
        c.setAdditionalPaths(new String[]{"USD", "EUR"});

        return new ApiRecord[]{a, b, c};
    }

    private void feedInput(String text) {
        System.setIn(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    private InteractiveUtils utils;

    @BeforeEach
    void setUp() {
        utils = new InteractiveUtils(makeApis());
    }

    @AfterEach
    void restoreStdin() {
        System.setIn(ORIGINAL_IN);
    }

    // askUserForApis (no filter)

    @Test
    void askUserForApis_singleDigit_returnsArray() {
        feedInput("1\n");
        int[] result = utils.askUserForApis("Pick: ");
        assertArrayEquals(new int[]{1}, result);
    }

    @Test
    void askUserForApis_commaSeparated_returnsIds() {
        feedInput("0,1,2\n");
        int[] result = utils.askUserForApis("Pick: ");
        assertArrayEquals(new int[]{0, 1, 2}, result);
    }

    @Test
    void askUserForApis_multiDigitIds_supported() {
        feedInput("10,13\n");
        int[] result = utils.askUserForApis("Pick: ");
        assertArrayEquals(new int[]{10, 13}, result);
    }

    @Test
    void askUserForApis_spacesAroundComma_accepted() {
        feedInput("1, 2\n");
        int[] result = utils.askUserForApis("Pick: ");
        assertArrayEquals(new int[]{1, 2}, result);
    }

    @Test
    void askUserForApis_all_returnsAllApiIds() {
        feedInput("all\n");
        int[] result = utils.askUserForApis("Pick: ");
        assertArrayEquals(new int[]{0, 1, 2}, result);
    }

    @Test
    void askUserForApis_emptyThenValid_retriesAndReturns() {
        feedInput("\n2\n");
        int[] result = utils.askUserForApis("Pick: ");
        assertArrayEquals(new int[]{2}, result);
    }

    @Test
    void askUserForApis_nonNumericThenValid_retriesAndReturns() {
        feedInput("abc\n0\n");
        int[] result = utils.askUserForApis("Pick: ");
        assertArrayEquals(new int[]{0}, result);
    }

    @Test
    void askUserForApis_spacesWithoutComma_retriesThenAccepts() {
        feedInput("1 2\n1,2\n");
        assertArrayEquals(new int[]{1, 2}, utils.askUserForApis("Pick: "));
    }

    @Test
    void askUserForApis_singleSpace_retriesThenAccepts() {
        feedInput(" \n0\n");
        assertArrayEquals(new int[]{0}, utils.askUserForApis("Pick: "));
    }

    @Test
    void askUserForApis_trailingComma_retriesThenAccepts() {
        feedInput("1,\n2\n");
        assertArrayEquals(new int[]{2}, utils.askUserForApis("Pick: "));
    }

    // askUserForApis (with filter)

    @Test
    void askUserForApisFiltered_none_returnsEmpty() {
        feedInput("none\n");
        int[] result = utils.askUserForApis("Pick output: ", new int[]{0, 1, 2});
        assertArrayEquals(new int[]{}, result);
    }

    @Test
    void askUserForApisFiltered_all_returnsAllApiIds() {
        feedInput("all\n");
        int[] result = utils.askUserForApis("Pick output: ", new int[]{0, 1, 2});
        assertArrayEquals(new int[]{0, 1, 2}, result);
    }

    @Test
    void askUserForApisFiltered_digit_returnsIt() {
        feedInput("1\n");
        int[] result = utils.askUserForApis("Pick output: ", new int[]{0, 1, 2});
        assertArrayEquals(new int[]{1}, result);
    }

    @Test
    void askUserForApisFiltered_emptyThenValid_retries() {
        feedInput("\n0\n");
        int[] result = utils.askUserForApis("Pick: ", new int[]{0, 1, 2});
        assertArrayEquals(new int[]{0}, result);
    }

    // askUserWhetherToAppend

    @Test
    void askUserWhetherToAppend_0_returnsOverwrite() {
        feedInput("0\n");
        WriteMode mode = utils.askUserWhetherToAppend();
        assertEquals(WriteMode.OVERWRITE, mode);
    }

    @Test
    void askUserWhetherToAppend_1_returnsAppend() {
        feedInput("1\n");
        WriteMode mode = utils.askUserWhetherToAppend();
        assertEquals(WriteMode.APPEND, mode);
    }

    @Test
    void askUserWhetherToAppend_invalidThenValid_retries() {
        feedInput("9\n0\n");
        WriteMode mode = utils.askUserWhetherToAppend();
        assertEquals(WriteMode.OVERWRITE, mode);
    }

    @Test
    void askUserWhetherToAppend_tooLongThenValid_retries() {
        feedInput("00\n1\n");
        WriteMode mode = utils.askUserWhetherToAppend();
        assertEquals(WriteMode.APPEND, mode);
    }

    @Test
    void askUserWhetherToAppend_letterThenValid_retries() {
        feedInput("x\n1\n");
        WriteMode mode = utils.askUserWhetherToAppend();
        assertEquals(WriteMode.APPEND, mode);
    }

    // askUserForFileType

    @Test
    void askUserForFileType_0_returnsJson() {
        feedInput("0\n");
        RunArgs.FileType type = utils.askUserForFileType();
        assertEquals(RunArgs.FileType.JSON, type);
    }

    @Test
    void askUserForFileType_1_returnsCsv() {
        feedInput("1\n");
        RunArgs.FileType type = utils.askUserForFileType();
        assertEquals(RunArgs.FileType.CSV, type);
    }

    @Test
    void askUserForFileType_outOfBoundsThenValid_retries() {
        feedInput("5\n0\n");
        RunArgs.FileType type = utils.askUserForFileType();
        assertEquals(RunArgs.FileType.JSON, type);
    }

    @Test
    void askUserForFileType_letterThenValid_retries() {
        feedInput("z\n1\n");
        RunArgs.FileType type = utils.askUserForFileType();
        assertEquals(RunArgs.FileType.CSV, type);
    }

    // askUserForAdditionalPath

    @Test
    void askUserForAdditionalPath_validIndex_returnsCorrectPath() {
        ApiRecord movies = makeApis()[1]; // paths: popular, top_rated
        feedInput("1\n");
        String path = utils.askUserForAdditionalPath(movies);
        assertEquals("popular", path);
    }

    @Test
    void askUserForAdditionalPath_secondIndex_returnsSecondPath() {
        ApiRecord movies = makeApis()[1];
        feedInput("2\n");
        String path = utils.askUserForAdditionalPath(movies);
        assertEquals("top_rated", path);
    }

    @Test
    void askUserForAdditionalPath_random_returnsOneOfPaths() {
        ApiRecord movies = makeApis()[1];
        feedInput("random\n");
        String path = utils.askUserForAdditionalPath(movies);
        assertTrue(path.equals("popular") || path.equals("top_rated"),
                "Unexpected path: " + path);
    }

    @Test
    void askUserForAdditionalPath_outOfBoundsThenValid_retries() {
        ApiRecord movies = makeApis()[1]; // 2 paths
        feedInput("99\n1\n");
        String path = utils.askUserForAdditionalPath(movies);
        assertEquals("popular", path);
    }

    @Test
    void askUserForAdditionalPath_letterThenValid_retries() {
        ApiRecord movies = makeApis()[1];
        feedInput("abc\n2\n");
        String path = utils.askUserForAdditionalPath(movies);
        assertEquals("top_rated", path);
    }

    @Test
    void askUserForAdditionalPath_zeroIndex_retriesThenAccepts() {
        ApiRecord movies = makeApis()[1];
        feedInput("0\n1\n");
        assertEquals("popular", utils.askUserForAdditionalPath(movies));
    }

    @Test
    void askUserForAdditionalPath_lastValidIndex_returnsLastPath() {
        ApiRecord movies = makeApis()[1]; // 2 paths
        feedInput("2\n");
        assertEquals("top_rated", utils.askUserForAdditionalPath(movies));
    }

    // askUserForMaxConcurrent

    @Test
    void askUserForMaxConcurrent_validPositive_returnsValue() {
        feedInput("3\n");
        int n = utils.askUserForMaxConcurrent();
        assertEquals(3, n);
    }

    @Test
    void askUserForMaxConcurrent_zeroThenValid_retries() {
        feedInput("0\n2\n");
        int n = utils.askUserForMaxConcurrent();
        assertEquals(2, n);
    }

    @Test
    void askUserForMaxConcurrent_negativeThenValid_retries() {
        feedInput("-5\n4\n");
        int n = utils.askUserForMaxConcurrent();
        assertEquals(4, n);
    }

    @Test
    void askUserForMaxConcurrent_letterThenValid_retries() {
        feedInput("abc\n1\n");
        int n = utils.askUserForMaxConcurrent();
        assertEquals(1, n);
    }

    @Test
    void askUserForMaxConcurrent_exactly1_accepted() {
        feedInput("1\n");
        assertEquals(1, utils.askUserForMaxConcurrent());
    }

    @Test
    void askUserForMaxConcurrent_largeValue_accepted() {
        feedInput("100\n");
        assertEquals(100, utils.askUserForMaxConcurrent());
    }

    // askUserForInterval

    @Test
    void askUserForInterval_validPositive_returnsValue() {
        feedInput("10\n");
        int t = utils.askUserForInterval();
        assertEquals(10, t);
    }

    @Test
    void askUserForInterval_zeroThenValid_retries() {
        feedInput("0\n5\n");
        int t = utils.askUserForInterval();
        assertEquals(5, t);
    }

    @Test
    void askUserForInterval_negativeThenValid_retries() {
        feedInput("-1\n7\n");
        int t = utils.askUserForInterval();
        assertEquals(7, t);
    }

    @Test
    void askUserForInterval_letterThenValid_retries() {
        feedInput("xyz\n15\n");
        int t = utils.askUserForInterval();
        assertEquals(15, t);
    }

    @Test
    void askUserForInterval_exactly1_accepted() {
        feedInput("1\n");
        assertEquals(1, utils.askUserForInterval());
    }

    @Test
    void askUserForInterval_decimalThenValid_retries() {
        feedInput("1.5\n3\n");
        assertEquals(3, utils.askUserForInterval());
    }

    // stop-loop pattern (mirrors UserInteraction.runInteractive)

    @Test
    void stopLoop_typingStop_terminatesLoop() throws Exception {
        assertLoopStopsOn("junk\nstop\n");
    }

    @Test
    void stopLoop_allCapsStop_terminatesLoop() throws Exception {
        assertLoopStopsOn("STOP\n");
    }

    @Test
    void stopLoop_mixedCaseStop_terminatesLoop() throws Exception {
        assertLoopStopsOn("Stop\n");
    }

    @Test
    void stopLoop_stopWithWhitespace_terminatesLoop() throws Exception {
        assertLoopStopsOn("  stop  \n");
    }

    private void assertLoopStopsOn(String input) throws Exception {
        Callable<Boolean> loopTask = () -> {
            feedInput(input);
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().trim().equalsIgnoreCase("stop")) return true;
            }
            return false;
        };
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            assertTrue(exec.submit(loopTask).get(2, TimeUnit.SECONDS),
                    "Loop should have exited on 'stop' input");
        } finally {
            exec.shutdownNow();
        }
    }
}
