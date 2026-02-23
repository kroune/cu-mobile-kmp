import UIKit
import SwiftUI
import ComposeApp

struct RootView: UIViewControllerRepresentable {
    let root: DefaultRootComponent

    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.rootViewController(root: root)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
