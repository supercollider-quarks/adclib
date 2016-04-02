Nudge {
	var <>object, <>key, <>getFunc, <>setFunc;
	var <>map2BiFunc, <>nudgeFunc, <>borderFunc, <>unmapFunc;
	var <>state, <unival, <bival;
	*new { |obj, key|
		^super.newCopyArgs(obj, key).init;
	}
	init {
		state = (dir: 1, border: 1.0);
		getFunc = { |obj, key| obj.getUni(key) };
		setFunc = { |obj, key, val| obj.setUni(key, val) };
		nudgeFunc = { |val, delta| (val + (delta * state[\dir])) };

		this.useLin;
		this.useFold;
	}

	nudge { |delta|
		unival = getFunc.(object, key);
		bival = map2BiFunc.(unival);
		bival = nudgeFunc.(bival, delta);
		bival = borderFunc.(bival) ? bival;
		unival = unmapFunc.(bival, state);
		setFunc.value(object, key, unival);
	}

	useClip {
		borderFunc = { |val| val.clip2(state[\border]) };
	}
	useWrap {
		borderFunc = { |val| val.wrap2(state[\border]) };
	}

	useFold {
		borderFunc = { |val|
			if (val.abs >= state[\border]) {
				state[\dir] = state[\dir].neg;
				val = val.fold2(state[\border]);
			} { val };
		};
	}

	useLin {
		state[\border] = 1.0;
		map2BiFunc = { |val| val.unibi };
		borderFunc = { |val| val.clip2(state[\border]) };
		unmapFunc = { |val| val.biuni };
	}

	useTan { |drive = 5|
		state[\border] = drive.atan;
		state[\tanGain] = drive.atan;
		map2BiFunc = { |val| (val.unibi * state[\tanGain]).tan };
		unmapFunc = { |val| (val.atan / state[\border]).biuni };
	}

	useSin {
		state[\border] = 0.5pi;
		state[\tanGain] = 1;
		map2BiFunc = { |val| val.unibi.asin };
		unmapFunc = { |val| val.sin.biuni };
	}


	useBipow { |exp = 0.5|
		state[\border] = 1;
		state[\exp] = exp;
		map2BiFunc = { |val| val.unibi.bipow(state[\exp]) };
		unmapFunc = { |val| val.bipow(state[\exp].reciprocal).biuni };
	}

		// FIXME
	// useAtan { |drive = 5|
	// 	state[\tanGain] = drive;
	// 	state[\border] = drive.atan;
	// 	map2BiFunc = { |val| (val.unibi * state[\tanGain]).atan };
	// 	unmapFunc = { |val| (val.tan / state[\border]).biuni };
	// }

}