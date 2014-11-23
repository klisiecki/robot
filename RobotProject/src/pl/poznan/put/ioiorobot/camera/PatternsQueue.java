package pl.poznan.put.ioiorobot.camera;

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
	private PatternsWidget patternsWidget;

	public PatternsQueue() {
		patterns = new LinkedList<Pattern>();
	}

	public void add(Pattern newPattern) {
		Iterator<Pattern> iterator = patterns.iterator();
		boolean inserted = false;

		while (iterator.hasNext()) {
			Pattern p = iterator.next();
			Log.d("robot", p.getId() + "|"+newPattern.getId()+ " Pokrycie = " + p.compareTo(newPattern));
			if (p.compareTo(newPattern) > C.minPatternCoverage) {
				Log.d("robot", "\t" + p.getId() + " count = " + p.getCount());
				inserted = true;
				if (p.incrementCount() == C.minPatternCount) {
					accept(p);
					Log.d("robot", "\t\t" + newPattern.getId() + " -> " + p.getId());
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
		//toRemove.add(pattern);
		patternAcceptedListener.onPatternAccepted(pattern);
	}
}
