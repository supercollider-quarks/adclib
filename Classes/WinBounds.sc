
WinBounds {
	classvar <stored;
	classvar <makeFuncs;
	classvar <>postMissingBounds = false;

	*initClass {
		stored = (); // NamedList.new;
		makeFuncs = ();
	}

	*storeAll { Window.allWindows.do(this.storeWin(_)) }

	*restoreAll { Window.allWindows.do(this.restoreWin(_)) }

	*addMake { |name, func| makeFuncs.put(name, func) }
	*make { |name| ^makeFuncs[name].value(name) }

	*showOrMake { |name, restore = true, finishFunc|
		var win = Window.find(name = name.asSymbol);

		if (win.isNil or: { win.isClosed }) {
			win = WinBounds.make(name);
		};
		if (restore) {
			WinBounds.restoreWin(win)
		};
		win.front;
		finishFunc.value(win);
	}

	*saveString {
		var storedStr = WinBounds.stored.cs
		.replace(" '", "\n\t'")
		.replace(") )", ")\n)");

		^"WinBounds.stored.putAll( % );\n".format(storedStr);
	}

	*store { |name|
		^this.storeWin(Window.find(name))
	}

	*restore { |name|
		^this.restoreWin(Window.find(name))
	}

	*put { |name, bounds|
		stored.put(name.asSymbol, bounds)
	}

	*at { |key|
		^stored[key.asSymbol]
	}

	*putAll { |coll|
		coll.keysValuesDo { |key, val|
			this.put(key, val)
		}
	}

	*storeWin { |win|
		if (win.isNil) {
			"WinBounds: cannot store bounds for nil.".postln;
			^this
		};
		this.put(win.name, win.bounds)
	}

	*restoreWin { |win|
		var found;
		if (win.isNil) {
			"WinBounds: cannot restore bounds for nil.".postln;
			^this
		};
		found = WinBounds.at(win.name.asSymbol);
		if (found.isNil) {
			if (postMissingBounds) {
				"WinBounds: no bounds found for window %.\n".postf(win.name.cs);
			};
			^this
		};
		win.bounds_(found);
	}
}
