
Butz {
	classvar <w, <actions, <butz, <style, <>numButz = 12;
	classvar <defBounds;

	*initClass {
		style = (
			winCol: Color.red,
			name: "Butz",
			font: Font(Font.defaultSansFace, 16),
			fontCol: Color.white,
			butCol: Color.yellow(1.0, 0.3),
			winLoc: 5@50,
			winExtent: 110@40, // minimize
			margins: [0,0],
			spacing: 0
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
	// convenience for small/big flip
	*addMiniMax {
		Butz.actions.addFirst(\miniMax, {
			var numToShow = if (Butz.butz[1].visible, 1, nil);
			Butz.showButs(numToShow);
		})
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
		if (w.notNil and: { w.isClosed.not }) {
			try { w.front };
			^this
		};
		this.makeWin;
	}

	*checkFontSize {
		// estimate layout height, and reduce font size if needed
		var maxbutheight = (Window.screenBounds.height - 24 / (Butz.actions.size));
		var maxfontsize = (maxbutheight / 2 - style.spacing).asInteger;
		if (style.font.size > maxfontsize) {
			"Butz: reduce fontsize to %".postf(maxfontsize);
			style.font.size = maxfontsize;
			butz.do(_.font_(style.font))
		}
	}

	*makeWin {
		var style = Butz.style;
		var winLocX = style.winLoc.x;
		var winLocY = style.winLoc.y;
		var initRect = Window.flipY(Rect(winLocX, winLocY, style.winExtent.x, style.winExtent.y));
		var win = Window(style.name, initRect);
		var numB = max(Butz.numButz, Butz.actions.size);

		w = win;
		w.alwaysOnTop_(true).userCanClose_(false);
		w.background_(style.winCol);
		w.layout = VLayout(
			*(butz = numB.collect {
				Button(w).states_([this.blankState])
				.font_(style.font)
			})
		);
		w.layout.margins_(style.margins);
		w.layout.spacing_(style.spacing);

		win.onClose = { w = nil };

		this.updateButtons.showButs;

		^win.front;
	}

	*showButs { |butsToShow, wait = 0.01|
		var bnds = Butz.w.bounds;
		// flipped, so this stores orig top
		var origBottom = bnds.bottom;
		butsToShow = butsToShow ? Butz.butz.size;
		fork ({
			Butz.butz.do { |but, i|
				but.visible_(i < butsToShow);
			};
			wait.wait;
			// make it small, so it gets minimal size
			// for its number of butsToShow:
			Butz.w.bounds_(
				bnds.extent_(Butz.style.winExtent)
			);
			// now it has its minimum size,
			// move it to its previous top,
			// and limit to screen height
			Butz.w.bounds_(
				WinBounds.limitRectToScreen(
					// flipped, so this restores orig top
					Butz.w.bounds.bottom_(origBottom)
				)
			)
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

		defer { but.states_(but.states) };
		but.action = action;
	}

	*updateButtons {
		this.checkFontSize;
		butz.do { |bt, i| this.setButton(i) }
	}
}
