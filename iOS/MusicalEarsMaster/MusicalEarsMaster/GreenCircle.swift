//
//  GreenCircle.swift
//  MusicalEarsMaster
//
//  Created by Jack Marty on 2/4/20.
//  Copyright © 2020 Jack Marty. All rights reserved.
//

import UIKit

class GreenCircle: UIButton {

    override func draw(_ rect: CGRect) {
      let path = UIBezierPath(ovalIn: rect)
      UIColor(red: 63/255, green: 191/255, blue: 4/255, alpha: 1).setFill()
      path.fill()
    }

}
