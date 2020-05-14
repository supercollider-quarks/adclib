
Butz {
	classvar <w, <actions, <butz, <style, <>numButz = 12;
	classvar <defBounds;

	*initClass {
		style = (
			winCol: Color.red,
			name: "Butz",
			font: Font("Monaco", 18),
			fontCol: Color.white,
			butCol: Color.yellow(1.0, 0.3),
			winLoc: 5@30,
			winExtent: 120@120
		);
		actions = NamedList();
		defBounds = ();
	}

	*run { |name| actions[name].value }

	*add { |name, action|
		if (name.isNil) {
		//	"Butz.add: cannot add action without name, so ignored.\n"
			^this
		};
		if (action.isNil and: actions[name].notNil) {
			// "Butz.add: protects from overwriting an existing action with nil, so ignored.\n"
			// "to remove an action, use Butz.remove(<name>).".postln;
			^this
		};

		actions.add(name, action);
		if (butz.notNil) {
			this.setButton(actions.names.indexOf(name));
		}
	}

	*remove { |name|
		actions.removeAt(name);
		if (butz.notNil) { this.updateButtons }
	}

	*clear {
		actions.clear;
		this.updateButtons;
	}

	*show {
		if (w.notNil) {
			try { w.front };
			^this
		};
		this.makeWin;
	}

	*makeWin {
		var style = Butz.style;
		var win = Window(style.name, style.winExtent.asRect);
		var numB = max(Butz.numButz, Butz.actions.size);
		var winLocX = style.winLoc.x;
		var winLocY = Window.screenBounds.height - style.winLoc.y;

		w = win;
		w.alwaysOnTop_(true).userCanClose_(false);
		w.background_(style.winCol);
		w.layout = VLayout(
			*(butz = numB.collect {
				Button(w).states_([this.blankState]).font_(style.font)
			})
		);

		^win.moveTo(winLocX, winLocY).front;
	}

	*showButs { |butsToShow, wait = 0.01|
		var bnds = Butz.w.bounds;
		var bottom = bnds.bottom;
		var left = bnds.left;
		butsToShow = butsToShow ? Butz.butz.size;
		fork ({
			Butz.butz.do { |but, i|
				but.visible_(i < butsToShow);
			};
			wait.wait;
			Butz.w.bounds_(Rect(left, bottom, 120, 24));
		}, AppClock);
	}

	*blankState { ^[ " . . . ", style.fontCol, style.butCol ] }

	*setButton { |index|
		var but, name, action;

		but = butz[index];
		but ?? {
			"*** %: no button at index % for %!\n".postf(this, index, name.cs);
			^this
		};
		name = actions.names[index] ? " . . . ";
		action = action ? actions[index];

		but.states.do { |state| state.put(0, name) };
		but.states_(but.states);
		but.action = action;
	}

	*updateButtons {
		butz.do { |bt, i| this.setButton(i) }
	}
}
