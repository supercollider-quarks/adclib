
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
			winLoc: 5@500,
			winExtent: 150@400,

		);
		actions = NamedList();
		defBounds = ();
	}

	*run { |name| actions[name].value }

	*add { |name, action|
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
		this.updateButtons;
	}

	*makeWin {
		var style = Butz.style;
		var rect = Rect.fromPoints( style.winLoc, style.winLoc + style.winExtent);
		var win = Window(style.name, rect).front;
		var numB = max(Butz.numButz, Butz.actions.size);
		w = win;
		w.alwaysOnTop_(true).userCanClose_(false);
		w.background_(style.winCol);
		w.layout = VLayout(
			*(butz = numB.collect {
				Button(w).states_([this.blankState]).font_(style.font)
			})
		);
		this.updateButtons;
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
