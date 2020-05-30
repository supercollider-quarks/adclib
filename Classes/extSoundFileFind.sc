///// find 4 default sounds in SC resourceDir:
// SoundFile.pathMatch(Platform.resourceDir +/+ "sounds/a11*.aiff");
// SoundFile.pathMatch("~/*/*.*").size;
// SoundFile.pathMatch("~/*/*/*.*").size;

/*

// three intended fails:
SoundFile.exists("nope/no/file/there.wav");
SoundFile.exists("this/is/a/folder/");
SoundFile.exists("this/is/a/forbidden.psd");
// psd open bug says something
SoundFile.openRead("/Users/adc/Pictures/DragonFyrAll.psd");

// true:
SoundFile.exists(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff");

SoundFile.openRead("/Users/adc/Pictures/DragonFyrAll.psd")
SoundFile.pathMatch("/Users/adc/Pictures/DragonFyrAll.psd")
SoundFile.pathMatch(Platform.resourceDir +/+ "sounds/a11*.aiff");
SoundFile.find([Platform.resourceDir +/+ "sounds/a11*.aiff"]).flat;
SoundFile.find(Platform.resourceDir +/+ "sounds/""*").flat;

SoundFile.find(Platform.resourceDir +/+ "sounds/""*", { |sf| sf.duration > 1 }).flat;
SoundFile.find(Platform.resourceDir +/+ "sounds/""*", { |sf| sf.duration <= 1 }).flat;

*/

+ SoundFile {

	// find all soundfiles at path patterns
	// that pass the test:
	*find { |paths, test = true|
		if (paths.isKindOf(String)) { paths = paths.bubble };
		^paths.collect { |pathpat|
			pathpat.pathMatch.collect { |path|
				SoundFile(path);
			}.select { |sndfile|
				sndfile.openRead and: {
					test.value(sndfile)
				}
			}
		}
	}

	*pathMatch { |path|
		^path.pathMatch.select(this.exists(_))
	}

	*exists { |path|
		var sf, exists;
		if (path.last == Platform.pathSeparator) {
			^false
		};
		// SoundFile crashes when trying to read a psd!
		if (path.splitext.last == "psd") {
			// "*** evil PSD!".postln;
			// path.postcs;
			^false
		};
		sf = this.new(path);
		exists = sf.openRead;
		if (sf.isOpen) { sf.close };
		^exists
	}
}
