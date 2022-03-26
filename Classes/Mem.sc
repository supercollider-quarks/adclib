Mem {
	classvar <>minSize = 3;
	var <>maxMems, <used;

	*new { |maxMems = 50|
		^this.newCopyArgs(maxMems, List[])
	}

	add { |item|
		// remove if already there, add first, drop oldest
		used.remove(item);
		used = used.addFirst(item);
		while { used.size > maxMems } { used.pop }
	}

	filter { |list, minSize|
		var res = list.copy;
		minSize = minSize ? this.class.minSize;
		used.do { |item|
			if (res.size <= minSize) { ^res };
			res.remove(item)
		};
		^res
	}

	filterDict { |dict, minSize|
		var res = dict.copy;
		minSize = minSize ? this.class.minSize;
		used.do { |key|
			if (res.size <= minSize) { ^res };
				res.removeAt(key)
		};
		^res
	}
}
