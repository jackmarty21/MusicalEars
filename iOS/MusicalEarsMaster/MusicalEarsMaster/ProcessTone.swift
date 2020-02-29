//
//  ProcessTone.swift
//  MusicalEarsMaster
//
//  Created by Jack Marty on 2/12/20.
//  Copyright © 2020 Jack Marty. All rights reserved.
//

import Foundation

class ProcessTone {
    
    func getBaseFrequency(frequency: Float, targetArray: [Note]) -> Float {
        
        var baseFrequency = frequency
        
        while baseFrequency > Float(targetArray[targetArray.count - 1].frequency) {
            baseFrequency /= 2.0
        }
        while baseFrequency < Float(targetArray[0].frequency) {
            baseFrequency *= 2.0
        }
        
        return baseFrequency
    }
    
    func getMeasuredFreqIndex(targetArray: [Note], roundedFrequency: Float?) -> Int {
        
        //minDistance identifies measured frequency
        var minDistance: Float = 10_000.0
        var index = 0
        
        //Identify the measured frequency note
        for i in 0..<targetArray.count {
            let distanceTest = fabsf(Float(targetArray[i].frequency) - roundedFrequency!)
            if distanceTest < minDistance {
                index = i
                minDistance = distanceTest
            }
        }
        
        return index
        
    }
    
    func getCents(roundedFrequency: Float!, targetArray: [Note]) -> Int{
        
        //distance = measured-target
        var distance: Float = 10_000.0
        var centValue: Float = 0.0
        var centAmount: Float = 0.0
        var centAmountInt: Int = 0
        
        distance = fabsf(Float(targetArray[5].frequency) - roundedFrequency!)
        
        //if measured frequency is greater than target note, move UI up
        if roundedFrequency! > Float(targetArray[5].frequency) {
            
            centValue = Float((targetArray[6].frequency-targetArray[5].frequency)/100)
            
            centAmount = distance / Float(centValue)
            centAmountInt = Int(centAmount)
            
        }
        //if measured frequency is less than target note, move UI down
        else if roundedFrequency! < Float(targetArray[5].frequency) {
            centValue = Float((targetArray[5].frequency-targetArray[4].frequency)/100)
            
            centAmount = distance / Float(centValue)
            centAmountInt = -Int(centAmount)
            
        }
        //If measured frequency is equal to target, then UI goes in the middle of the screen
        else {
            centAmountInt = 0
        }
        //print("cents = \(centAmountInt)")
        
        return centAmountInt
    }
    
    func getYCoordinate (initialCoordinate: Int, centAmountInt: Int, decrement: Int) -> Int {
        
        var y = initialCoordinate
        
        if centAmountInt > decrement {
            y = y - decrement
        }
        else if centAmountInt < -decrement {
            y = y + decrement
        }
        else if (centAmountInt < decrement && centAmountInt > 0) || (centAmountInt < 0 && centAmountInt > -decrement) {
            y = y - centAmountInt
        }
        else  {
            y = initialCoordinate
        }
        
        return y
    }
}
