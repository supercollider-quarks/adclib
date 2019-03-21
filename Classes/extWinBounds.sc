/*

~names = [\abc, \bcd, \cde];
~names.do {|name| Window(name).front };

Window.find(\a).front
Window.findAll(\b)
Window.findAll(\c)

Window.find(\a).moveTo(200).front;
Window.find(\bcd).moveTo(200, 200).front;
Window.findAll(\c).do(_.front);

~locs = Window.getAll.postcs;
Window.findAll(\c).do(_.moveTo(800.rand, 800.rand));
Window.setAll(~locs, true);

*/

+ Window {

	*find { |name|
		name = name.asString;
		^Window.allWindows.detect { |w| w.name.contains(name) };
	}
	*findAll { |name|
		name = name.asString;
		^Window.allWindows.select { |w| w.name.contains(name) };
	}

	*getAll {
		^Window.allWindows.collect { |win| [win.name, win.bounds] };
	}
	*setAll { |pairs, toFront = false|
		pairs.do { |pair|

			var win = Window.allWindows.detect { |w| w.name == pair[0] };
			pair.postcs;

			if (win.notNil) {
				win.bounds = pair[1];
				if (toFront) { win.front };
			};
		};
	}
	moveTo { |left, top|
		var bounds = this.bounds;
		this.bounds_(bounds.left_(left ? bounds.left).top_(top ? bounds.top))
	}
}