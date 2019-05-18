/*
TestNatLess.run(true, false)
*/
TestNatLess : UnitTest {

	test_equalFalse {
		this.assert("a1x".natLess("a1x").not, "equal strings are not less  by default.")
	}
	test_equalTrue {
		this.assert("123".natLess("123", true), "equal strings are less if equal flag is true.")
	}
	test_substring {
		this.assert("a1" natLess: "a1b", "substrings are less.")
	}
	test_alphabetic {
		this.assert("ab1c" natLess: "ab1x", "alphabetic sorting works.")
	}

	test_lessDigits {
		this.assert("999" natLess: "1001", "shorter number strings are less.")
	}
	test_lessDigitsMixed {
		this.assert("a_99_x23" natLess: "a_101_xyz", "shorter numbers within strings are less.")
	}

	test_sorting {
		this.assert(
			["10", "2", "1"].sort { |a, b| a.natLess(b) } == [ "1", "2", "10" ],
			"natLess sorts arrays of strings correctly."
		);
	}

}
