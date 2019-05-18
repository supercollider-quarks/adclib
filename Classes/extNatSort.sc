/*

//////// The problem: higher numbers with lower leading digits are sorted before lower numbers:
(1..11).collect(_.asString).scramble.postcs.sort.cs;
// -> [ "1", "10", "11", "2", "3", ... ]

////////// natLess can be used to sort "naturally":
(1..11).collect(_.asString).scramble.postcs.sort { |a, b| a.natLess(b) }.cs;
// [ "1", "2", "3", "4", "5", ... ]

/// basic tests - rerouting to String:<
"abc".natLess("abc");  // false by default
"abc".natLess("abc", true); // true if set

"abc".natLess("abcd"); // true because shorter
"abc".natLess("abx"); // true alpha

// only one string has numbers -> uses normal <
"abc".natLess("abc1"); // true
"abc1".natLess("abc"); // false


////////// real test cases:
// lower numbers are less
"1".natLess("2");
"11".natLess("12");
"123".natLess("1000"); // true
"123".natLess("124"); // true
"100".natLess("123"); // true
"abc2".natLess("abc10"); // true

// numbers are less than letters
"2".natLess("a"); // true
"ab2".natLess("abcd2"); // true
"a_2_b".natLess("a_10_b"); // true

// mixed letter/numbers: lower
"abc2".natLess("abc10");     // true
"abc12_d".natLess("abc110_a"); // true
"abc1230".natLess("abc11000");   // true
"c2X" natLess: "c21X"

["10", "2", "1"].sort { |a, b| a.natLess(b) }.cs;

[ "ch1X", "ch21X", "ch2X" ].sort({ |a, b| a.natLess(b) });
[ "c1X", "c21X", "c2X" ].sort({ |a, b| a.natLess(b) });
[ "c1X", "c21X", "c2X" ].sort({ |a, b| a.natLess(b) });


*/

+ String {
	natLess { |str2, equal = false|
		var index = 0, char1, char2, lastCommonChar;
		var isDig1, isDig2, numStr1 = "", numStr2 = "";

		// speedups by early exit:
		/// for equal strings
		if (this == str2) { ^equal };

		/// just compare digit-free strings
		if (this.any(_.isDecDigit).not or: {
			str2.any(_.isDecDigit).not
		}) { ^this < str2 };

		// if strings begin the same, shorter is less
		while {
			char1 = this[index];
			if (char1.isNil) { ^true };
			char2 = str2[index];
			if (char2.isNil) { ^false };

			char1 == char2;
		} {
			lastCommonChar = char1;
			index = index + 1;
		};

		// now we know we already have two different chars

		while {
			if (lastCommonChar.notNil and: { lastCommonChar.isDecDigit }) {
				numStr1 = numStr1 ++ lastCommonChar;
				numStr2 = numStr2 ++ lastCommonChar;
			};

			char1 = this[index];
			char2 = str2[index];

			// lots of exits
			if (char1.notNil) {
				if (char2.isNil) { ^false };
				isDig1 = char1.isDecDigit;
				isDig2 = char2.isDecDigit;
				if (isDig1) { numStr1 = numStr1 ++ char1 };
				if (isDig2) { numStr2 = numStr2 ++ char2 };

				if (numStr1.size == 0) {
					if (isDig1 and: { isDig2.not }) { ^true };
					if (isDig2 and: { isDig1.not }) { ^false };
				};
				if (isDig1.not and: { isDig2.not }) {
					// "both nondigits".postln;
					if (numStr1.size > 0) {
						// stringcompare numstrings if present
						^numStr1 < numStr2
					};
					// if nonstrings, compare latest char
					^char1 < char2
				};
			} {
				// this shorter than str2: this is less
				if (char2.notNil) { ^true }
			};

			// one numString shorter : wins
			if (numStr1.size < numStr2.size) { ^true };
			if (numStr1.size > numStr2.size) { ^false };

			// keep going as long as both are notNil and numbers
			(isDig1 and: isDig2) and: { char1.notNil and: char2.notNil };
		} {
			index = index + 1;
		};

		// "// fallback - convert strings to number - should not happen.".postln;
		^numStr1.asInteger < numStr2.asInteger
	}
}
