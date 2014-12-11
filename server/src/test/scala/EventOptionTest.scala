
class EventOptionTest extends org.scalatest.FunSuite {
  
  test("The extractor should return All for input string 'all'") {
    val EventOption(opt) = "all"
    assert(opt == All)
  }
  
  test("The extractor should return SinceLast for input string 'latest'") {
    val EventOption(opt) = "latest"
    assert(opt == SinceLast)
  }
  
  test("The extractor should return Fixed for input string of number") {
    val EventOption(opt) = "1234"
    assert(opt == Fixed(1234))
  }
  
  test("The extractor should return All for invalid option") {
    val EventOption(opt) = "hello"
    assert(opt == All)
  }
}
