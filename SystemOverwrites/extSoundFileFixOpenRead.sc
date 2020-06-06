/*
// this old Photoshop file has a resource fork, which hangs libsndfile 1.0.28!

~pathtobadfile = "/Users/adc/Pictures/DragonFyrAll.psd";

SoundFile.openRead(~pathtobadfile);
-> Therefor, catch it here until a libsndfile update fixes this.

// TODO: should probably catch all files with resource fork.
// first pass, String:hasResourceFork is really slow:

// ca 5.4 secs, so 0.11 sec per test
bench {
	50.do {
		~pathtobadfile.pathHasResourceFork
	};
}
*/

+ String {
	pathHasResourceFork {
		^("xattr %".format(this.quote)).unixCmdGetStdOut.contains("ResourceFork")
	}
}

+ SoundFile {
	// temp fix for SoundFile bug: avoid trying to open .psd files
	openRead { arg pathName, postExcluded = false, fileExtsToExclude;
		var fileext;
		fileExtsToExclude = fileExtsToExclude ? ["pic", "psd"];
		path = pathName ? path;
		Platform.case(
			\osx, {
				fileext = path.splitext.last;
				if (fileext.notNil) {
					fileExtsToExclude.do { |badExt|
						if (fileext.containsi(badExt)) {
							if (postExcluded) {
								"*** openRead - excluded file %\n".
								postf(path.cs);
							};
							^false
						}
					}
				}
			}
		);
		^this.prOpenRead(path);
	}
}
