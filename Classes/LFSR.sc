/*
Simple implementation of a Linear Feedback Shift Register,
as proposed by Niko LFO.

a = LFSR.new;
10.do { a.next };
a.all;
a = LFSR.new(5, [0, 2, 4]);
a.all.size;
a.nextN(10);

a = LFSR.new(7, [0, 2, 4, 5]);
a.all.size;
a.all;
*/
LFSR {
	var <length, <taps, <values, <startValues;
	var <lastTaps, <reverseTaps;

	*new { |length = 3, taps = #[0, 2]|
		^super.newCopyArgs(length, taps).init
	}

	init {
		values = Array.fill(length, 0).put(taps, 1);
		startValues = values.copy;
		lastTaps = taps.keep(-2);
		reverseTaps = taps.drop(-2).reverse;
	}

	next {
		var xorvalue = absdif(values[lastTaps[0]], values[lastTaps[1]]);
		reverseTaps.do { |index|
			xorvalue = absdif(values[index], xorvalue)
		};
		values = values.rotate(1).put(0, xorvalue);
		^xorvalue
	}

	nextN { |num = 100|
		^num.collect (this.next)
	}

	// create all values at once
	all { |max = 10000|
		var results = [this.next];
		while { values != startValues and: { results.size < 10000} } {
			results = results.add(this.next);
		};
		^results
	}
}

