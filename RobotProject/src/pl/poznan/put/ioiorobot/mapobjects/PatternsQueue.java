package pl.poznan.put.ioiorobot.mapobjects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pl.poznan.put.ioiorobot.utils.C;
import pl.poznan.put.ioiorobot.widgets.PatternsWidget;
import android.util.Log;

public class PatternsQueue {

	public interface PatternAcceptedListener {
		void onPatternAccepted(Pattern pattern);
	}

	private PatternAcceptedListener patternAcceptedListener;

	public void setPatternAcceptedListener(PatternAcceptedListener patternAcceptedListener) {
		this.patternAcceptedListener = patternAcceptedListener;
	}

	private LinkedList<Pattern> patterns;
	private List<Pattern> toRemove = new ArrayList<Pattern>();

	public PatternsQueue() {
		patterns = new LinkedList<Pattern>();
	}

	public void add(Pattern newPattern) {
		boolean inserted = false;

		for (Pattern p: patterns) {
//			Log.d(C.TAG, p.getId() + "|"+newPattern.getId()+ " Pokrycie = " + p.compareTo(newPattern));
			if (p.compareTo(newPattern) > C.minPatternCoverage) {
				p.merge(newPattern);
//				Log.d(C.TAG, "\t" + p.getId() + " count = " + p.getCount());
				inserted = true;
				if (p.incrementCount() == C.minPatternCount) {
					accept(p);
//					Log.d(C.TAG, "\t\t" + newPattern.getId() + " -> " + p.getId());
				}
			}
			if (!p.check()) {
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
}
