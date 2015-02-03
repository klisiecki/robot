package pl.poznan.put.ioiorobot.mapping;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pl.poznan.put.ioiorobot.utils.Config;

/**
 * "Kolejka" wzorców, służy do odfiltrowania przypadkowo wykrytych wzorców nie
 * będących markerami
 */
public class PatternsQueue {

	private LinkedList<Pattern> patterns;
	private List<Pattern> toRemove = new ArrayList<Pattern>();
	private PatternAcceptedListener patternAcceptedListener;

	public PatternsQueue() {
		patterns = new LinkedList<Pattern>();
	}

	/**
	 * Dodaje znaleziony pattern do kolejki
	 */
	public void add(Pattern newPattern) {
		boolean inserted = false;

		int fill = newPattern.getFill();
		if (fill < Config.minPatternFill || fill > Config.maxPatternFill) {
			return;
		}

		for (Pattern p : patterns) {
			if (p.compareTo(newPattern) > Config.minPatternCoverage) {
				p.merge(newPattern);
				inserted = true;
				if (p.incrementCount() == Config.minPatternCount) {
					accept(p);
				}
			}
			if (!p.checkTTL()) {
				toRemove.add(p);
			}
		}

		if (!inserted) {
			patterns.add(newPattern);
		}

		patterns.removeAll(toRemove);
		toRemove.clear();
	}

	private void accept(Pattern pattern) {
		patternAcceptedListener.onPatternAccepted(pattern);
	}
	
	public interface PatternAcceptedListener {
		void onPatternAccepted(Pattern pattern);
	}
	
	public void setPatternAcceptedListener(PatternAcceptedListener patternAcceptedListener) {
		this.patternAcceptedListener = patternAcceptedListener;
	}
}
