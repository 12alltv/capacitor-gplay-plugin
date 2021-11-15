import Foundation

@objc public class Gpay: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
