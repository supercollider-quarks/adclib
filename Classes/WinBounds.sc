
WinBounds {
	classvar <stored;

	*initClass {
		stored = ();
	}

	*storeAll { Window.allWindows.do(this.storeWin(_)) }

	*restoreAll { Window.allWindows.do(this.restoreWin(_)) }

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
		found = WinBounds.at(win.name.asSymbol.postcs);
		if (found.isNil) {
			"WinBounds: no bounds found for win.name.".postln;
			^this
		};
		win.bounds_(found);
	}
}
