//
//  Circle.swift
//  MusicalEarsMaster
//
//  Created by Jack Marty on 2/3/20.
//  Copyright © 2020 Jack Marty. All rights reserved.
//

import UIKit

class Circle: UIButton {

    override func draw(_ rect: CGRect) {
      let path = UIBezierPath(ovalIn: rect)
      UIColor(red: 92/255, green: 200/255, blue: 242/255, alpha: 1).setFill()
      path.fill()
    }
    
}
