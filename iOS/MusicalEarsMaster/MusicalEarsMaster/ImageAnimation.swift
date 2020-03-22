//
//  ImageAnimation.swift
//  MusicalEarsMaster
//
//  Created by Jack Marty on 3/4/20.
//  Copyright © 2020 Jack Marty. All rights reserved.
//

import UIKit

class ImageAnimation: UIViewController {

    @IBOutlet weak var greenLabel: UILabel!
    @IBOutlet weak var imageOn: UIImageView!
    @IBOutlet weak var imageOff: UIImageView!
    @IBOutlet weak var noteLabel: UILabel!
    @IBOutlet weak var targetNote: UILabel!
    @IBOutlet weak var blackLabelWidthConstraint: NSLayoutConstraint!
    @IBOutlet weak var greenLabelWidthConstraint: NSLayoutConstraint!
    @IBOutlet var myView: UIView!
    @IBOutlet weak var imageOffConstraint: NSLayoutConstraint!
    @IBOutlet weak var imageOnConstraint: NSLayoutConstraint!
    @IBOutlet weak var noteConstraint: NSLayoutConstraint!
    @IBOutlet weak var darkLabel: UILabel!
    @IBOutlet weak var checkMark: UIImageView!
    
        
    var myColors = Colors()
    var myNotes = Notes()
    var playSoundFiles = PlaySound()
    var processTone = ProcessTone()
    var micIsOn = false
    var timerDidStart = false

    var randomNote = Int()

    var height = CGFloat()
    var width = CGFloat()
    var labelWidth = CGFloat()


    override func viewDidLoad() {
       super.viewDidLoad()
       // Do any additional setup after loading the view.
        myView.layer.backgroundColor = UIColor( red: 0, green: 0, blue: 0, alpha: 0 ).cgColor
        imageOnConstraint.constant = 0
        imageOffConstraint.constant = 0
        noteConstraint.constant = 0
         
        noteLabel.center = CGPoint(x: 40, y: height/2)
        noteLabel.center = CGPoint(x: 40, y: height/2)
        imageOff.center = CGPoint(x: 40, y: height/2)
        
        targetNote.textColor = myColors.primaryDarkText
        darkLabel.backgroundColor = myColors.primaryDarkBorder
        
        checkMark.isHidden = true
       
    }
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

    }
    func animateImage(y: Int, centAmountInt: Int, amplitude: Double, baseAmplitude: Double, duration: Double) {
        
        if amplitude >= baseAmplitude {
            imageOnConstraint?.constant = CGFloat(y)
            imageOffConstraint?.constant = CGFloat(y)
            noteConstraint?.constant = CGFloat(y)
            UIView.animate(withDuration: 0.3, delay: 0.0, options: [], animations: {
                self.view.layoutIfNeeded()
            },
            completion: nil)
           
           //Start or Stop timer based off of cent value
           if abs(centAmountInt) <= 50 {
               greenLabelWidthConstraint.constant = labelWidth
               UIView.animate(withDuration: duration, delay: 0.2, options: [.curveLinear],
               animations: {
                   self.imageOff.alpha = 0
                   NSLayoutConstraint.deactivate([self.greenLabelWidthConstraint])
                   self.greenLabelWidthConstraint.constant = self.labelWidth
                   NSLayoutConstraint.activate([self.greenLabelWidthConstraint])
                   self.view.layoutIfNeeded()
               },
               completion: nil)

           } else if abs(centAmountInt) > 50 {
               UIView.animate(withDuration: 0.2, delay: 0.0, options: [.curveLinear],
               animations: {
                   self.imageOff.alpha = 1
                   NSLayoutConstraint.deactivate([self.greenLabelWidthConstraint])
                   self.greenLabelWidthConstraint?.constant = 0
                   NSLayoutConstraint.activate([self.greenLabelWidthConstraint])
                   self.view.layoutIfNeeded()
               },
               completion: nil)
           }
        } else {
           UIView.animate(withDuration: 0.2, delay: 0.0, options: [.curveLinear],
           animations: {
               self.imageOff.alpha = 1
               NSLayoutConstraint.deactivate([self.greenLabelWidthConstraint])
               self.greenLabelWidthConstraint.constant = 0
               NSLayoutConstraint.activate([self.greenLabelWidthConstraint])
               self.view.layoutIfNeeded()
           },
           completion: nil)
        }
    }
    func animateImageOff() {
        UIView.animate(withDuration: 0.2, delay: 0.0, options: [.curveLinear],
        animations: {
           self.imageOff.alpha = 1
           NSLayoutConstraint.deactivate([self.greenLabelWidthConstraint])
           self.greenLabelWidthConstraint.constant = 0
           NSLayoutConstraint.activate([self.greenLabelWidthConstraint])
           self.view.layoutIfNeeded()
        },
        completion: nil)
    }
    func blankScreenOn() {
        greenLabel.isHidden = true
        darkLabel.isHidden = true
        imageOn.isHidden = true
        imageOff.isHidden = true
        noteLabel.isHidden = true
    }
    func blankScreenOff() {
        greenLabel.isHidden = false
        darkLabel.isHidden = false
        imageOn.isHidden = false
        imageOff.isHidden = false
        noteLabel.isHidden = false
    }
}
