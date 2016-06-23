package nl.esciencecenter.e3dchem.sstea;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		assertEquals(20, scorer.getDistinctAA());
	}

	@Test
	public void testDistinctAA_includeGaps() {
		scorer = new Scorer(true);
		assertEquals(21, scorer.getDistinctAA());
	}

	@Test
	public void tofew_sequences() {
		Map<String, String> sequences = new HashMap<String, String>();
		sequences.put("seq1", "A");
		Set<String> subfamily_members = new HashSet<String>();

		thrown.expect(IllegalArgumentException.class);

		scorer.scoreit(sequences, subfamily_members);
	}

	@Test
	public void tofew_subfamily_members() {
		Set<String> subfamily_members = new HashSet<String>();

		thrown.expect(IllegalArgumentException.class);

		scorer.scoreit(sequences, subfamily_members);
	}

	@Test
	public void subfamily_members_notin_sequences() {
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq99");

		thrown.expect(IllegalArgumentException.class);

		scorer.scoreit(sequences, subfamily_members);
	}

	@Test
	public void conserved_subfamily() {
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq1");
		subfamily_members.add("seq2");
		subfamily_members.add("seq3");
		subfamily_members.add("seq4");

		List<Score> scores = scorer.scoreit(sequences, subfamily_members);

		ArrayList<Score> expected = new ArrayList<Score>();
		expected.add(new Score(1, 1.756026815012315, 0.5623351446188083, 1.3321790402101223, 2, 4));
		assertEquals(expected, scores);
	}

	@Test
	public void conserved_outsidesubfamily() {
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq5");
		subfamily_members.add("seq6");
		subfamily_members.add("seq7");
		subfamily_members.add("seq8");

		List<Score> scores = scorer.scoreit(sequences, subfamily_members);

		ArrayList<Score> expected = new ArrayList<Score>();
		expected.add(new Score(1, 2.4709766818493617, 1.3862943611198906, 0.9502705392332347, 4, 3));
		assertEquals(expected, scores);
	}

	@Test
	public void halfconserved_subfamily() {
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq1");
		subfamily_members.add("seq2");

		List<Score> scores = scorer.scoreit(sequences, subfamily_members);

		ArrayList<Score> expected = new ArrayList<Score>();
		expected.add(new Score(1, 1.2478641760872335, 0.0, 1.7478680974667573, 1, 6));
		assertEquals(expected, scores);
	}

	@Test
	public void halfconserved_outsidesubfamily() {
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq3");
		subfamily_members.add("seq4");
		subfamily_members.add("seq5");
		subfamily_members.add("seq6");

		List<Score> scores = scorer.scoreit(sequences, subfamily_members);

		ArrayList<Score> expected = new ArrayList<Score>();
		expected.add(new Score(1, 2.3850710020440697, 1.3862943611198906, 1.0549201679861442, 4, 3));
		assertEquals(expected, scores);
	}

	@Test
	public void conserved75_subfamily() {
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq1");
		subfamily_members.add("seq2");
		subfamily_members.add("seq3");
		subfamily_members.add("seq5");

		List<Score> scores = scorer.scoreit(sequences, subfamily_members);

		ArrayList<Score> expected = new ArrayList<Score>();
		expected.add(new Score(1, 1.9617412269422265, 1.0397207708399179, 1.3321790402101223, 3, 4));
		assertEquals(expected, scores);
	}

	@Test
	public void jaggedSequence() {
		sequences.put("seq10", "ICK");
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq1");
		List<Score> scores = scorer.scoreit(sequences, subfamily_members);
		assertEquals(scores.size(), 1);
	}

	@Test
	public void emptySequence() {
		sequences.put("seq10", "");
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq1");

		thrown.expect(IllegalArgumentException.class);

		scorer.scoreit(sequences, subfamily_members);
	}

	@Test
	public void includeGaps_seqWithGaps() {
		scorer = new Scorer(true);
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq1");
		subfamily_members.add("seq2");
		subfamily_members.add("seq3");
		subfamily_members.add("seq4");
		sequences.put("seq10", "-");

		List<Score> scores = scorer.scoreit(sequences, subfamily_members);

		ArrayList<Score> expected = new ArrayList<Score>();
		expected.add(new Score(1, 1.5867951825408655, 0.5623351446188083, 1.5607104090414063, 2, 5));
		assertEquals(expected, scores);
	}

	@Test
	public void includeGaps_seqWithoutGaps() {
		scorer = new Scorer(true);
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
	public void seqWithGapsOutsideFamily() {
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq1");
		subfamily_members.add("seq2");
		subfamily_members.add("seq3");
		subfamily_members.add("seq4");
		sequences.put("seq10", "-");

		List<Score> scores = scorer.scoreit(sequences, subfamily_members);

		ArrayList<Score> expected = new ArrayList<Score>();
		expected.add(new Score(1, 1.756026815012315, 0.5623351446188083, 1.3321790402101223, 2, 4));
		assertEquals(expected, scores);
	}

	@Test
	public void seqWithGapsInsideFamily() {
		Set<String> subfamily_members = new HashSet<String>();
		subfamily_members.add("seq1");
		subfamily_members.add("seq2");
		subfamily_members.add("seq3");
		sequences.put("seq10", "-");
		subfamily_members.add("seq10");

		List<Score> scores = scorer.scoreit(sequences, subfamily_members);

		ArrayList<Score> expected = new ArrayList<Score>();
		expected.add(new Score(1, 1.5698528714721045, 0.6365141682948128, 1.5607104090414063, 2, 5));
		assertEquals(expected, scores);
	}

}
