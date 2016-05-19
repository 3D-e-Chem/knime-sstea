package nl.esciencecenter.e3dchem.sstea;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.crypto.KeySelectorException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ScorerTest {
    private Scorer scorer;
    private HashMap<String, String> sequences;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        scorer = new Scorer();
        sequences = new HashMap<String, String>();
        sequences.put("seq1", "A");
        sequences.put("seq2", "A");
        sequences.put("seq3", "G");
        sequences.put("seq4", "A");
        sequences.put("seq5", "V");
        sequences.put("seq6", "L");
        sequences.put("seq7", "C");
        sequences.put("seq8", "I");
        sequences.put("seq9", "I");
    }

    @Test
    public void testDistinctAA_default() {
        assertEquals(21, scorer.getDistinctAA());
    }

    @Test
    public void tofew_sequences() throws KeySelectorException {
        Map<String, String> sequences = new HashMap<String, String>();
        sequences.put("seq1", "A");
        Set<String> subfamily_members = new HashSet<String>();

        thrown.expect(IllegalArgumentException.class);

        scorer.scoreit(sequences, subfamily_members);
    }

    @Test
    public void tofew_subfamily_members() throws KeySelectorException {
        Set<String> subfamily_members = new HashSet<String>();

        thrown.expect(IllegalArgumentException.class);

        scorer.scoreit(sequences, subfamily_members);
    }

    @Test
    public void subfamily_members_notin_sequences() throws KeySelectorException {
        Set<String> subfamily_members = new HashSet<String>();
        subfamily_members.add("seq99");

        thrown.expect(KeySelectorException.class);

        scorer.scoreit(sequences, subfamily_members);
    }

    @Test
    public void conserved_subfamily() throws KeySelectorException {
        Set<String> subfamily_members = new HashSet<String>();
        subfamily_members.add("seq1");
        subfamily_members.add("seq2");
        subfamily_members.add("seq3");
        subfamily_members.add("seq4");

        List<Score> scores = scorer.scoreit(sequences, subfamily_members);

        ArrayList<Score> expected = new ArrayList<Score>();
        expected.add(new Score(1, 1.80231537913897, 0.5623351446188083, 1.3321790402101223, 2, 4));
        assertEquals(expected, scores);
    }

    @Test
    public void conserved_outsidesubfamily() throws KeySelectorException {
        Set<String> subfamily_members = new HashSet<String>();
        subfamily_members.add("seq5");
        subfamily_members.add("seq6");
        subfamily_members.add("seq7");
        subfamily_members.add("seq8");

        List<Score> scores = scorer.scoreit(sequences, subfamily_members);

        ArrayList<Score> expected = new ArrayList<Score>();
        expected.add(new Score(1, 2.511514099104873, 1.3862943611198906, 0.9502705392332347, 4, 3));
        assertEquals(expected, scores);
    }

    @Test
    public void halfconserved_subfamily() throws KeySelectorException {
        Set<String> subfamily_members = new HashSet<String>();
        subfamily_members.add("seq1");
        subfamily_members.add("seq2");

        List<Score> scores = scorer.scoreit(sequences, subfamily_members);

        ArrayList<Score> expected = new ArrayList<Score>();
        expected.add(new Score(1, 1.2966543402566657, 0.0, 1.7478680974667573, 1, 6));
        assertEquals(expected, scores);
    }

    @Test
    public void halfconserved_outsidesubfamily() throws KeySelectorException {
        Set<String> subfamily_members = new HashSet<String>();
        subfamily_members.add("seq3");
        subfamily_members.add("seq4");
        subfamily_members.add("seq5");
        subfamily_members.add("seq6");

        List<Score> scores = scorer.scoreit(sequences, subfamily_members);

        ArrayList<Score> expected = new ArrayList<Score>();
        expected.add(new Score(1, 2.424939019319153, 1.3862943611198906, 1.0549201679861442, 4, 3));
        assertEquals(expected, scores);
    }

    @Test
    public void conserved75_subfamily() throws KeySelectorException {
        Set<String> subfamily_members = new HashSet<String>();
        subfamily_members.add("seq1");
        subfamily_members.add("seq2");
        subfamily_members.add("seq3");
        subfamily_members.add("seq5");

        List<Score> scores = scorer.scoreit(sequences, subfamily_members);

        ArrayList<Score> expected = new ArrayList<Score>();
        expected.add(new Score(1, 2.0032821050274836, 1.0397207708399179, 1.3321790402101223, 3, 4));
        assertEquals(expected, scores);
    }

    @Test
    public void jaggedSequence() throws KeySelectorException {
        sequences.put("seq10", "ICK");
        Set<String> subfamily_members = new HashSet<String>();
        subfamily_members.add("seq1");
        List<Score> scores = scorer.scoreit(sequences, subfamily_members);
        assertEquals(scores.size(), 1);
    }

    @Test
    public void emptySequence() throws KeySelectorException {
        sequences.put("seq10", "");
        Set<String> subfamily_members = new HashSet<String>();
        subfamily_members.add("seq1");

        thrown.expect(IllegalArgumentException.class);

        scorer.scoreit(sequences, subfamily_members);
    }

}
