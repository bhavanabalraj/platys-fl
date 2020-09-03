# -*- coding: utf-8 -*-

import pandas as pd
import torch
import torch.nn as nn
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import torch.nn.functional as F
from torch import optim
import math

# Utils
def transform(x):
 
    x = (x.split(' '))
    ans = [] 
    
    for i, data in enumerate(x):
        if not data == '':
            ans.append(float(data))
       
    return ans

# Classifier
class Network(nn.Module):
    
    def __init__(self):
        
        super().__init__()
        # Inputs to hidden layer linear transformation
        self.h1 = nn.Linear(561, 1000)
        self.h2 = nn.Linear(1000, 500)
              

        self.output = nn.Linear(500, 7)
        
    def forward(self, x):
               
        # Hidden layer with sigmoid activation
        x = F.dropout(F.relu(self.h1(x)))
        x = F.dropout(F.relu(self.h2(x)))
        
        # Output layer with softmax activation
        x = F.softmax(self.output(x), dim=1)
        
        return x

# Create model params and config
model = Network()

criterion = nn.CrossEntropyLoss()
optimizer = optim.SGD(model.parameters(), lr=0.003)

epochs = 100
train_losses, test_losses = [], []
batch_size = 32

#Train model
def trainAndTestModel(X_train, y_train, X_test, y_test):
    
    trainLoaderSize = len(X_train)/batch_size
    
    for e in range(epochs):
        
        running_loss = 0 
        
        permutation = torch.randperm(X_train.size()[0])
        
        for i in range(0, X_train.size()[0], batch_size):
            
            optimizer.zero_grad()
            
            indices = permutation[i:i+batch_size]
            batch_x, batch_y = X_train[indices], y_train[indices]
            
            log_ps = model(batch_x)
            loss = criterion(log_ps, batch_y)
            loss.backward()
            optimizer.step()
        
            running_loss += loss.item()
         
        else:
        
            train_losses.append(running_loss/trainLoaderSize)
            print("Epoch: {}/{}".format(e+1, epochs))   
            print("Training Loss: {:.3f}".format(running_loss/trainLoaderSize))
            validateModel(X_test, y_test)    
        
def validateModel(X_test, y_test):
            
        test_loss = 0
        accuracy = 0
        
        testLoaderSize = len(X_test)/batch_size

        permutation = torch.randperm(X_test.size()[0])
    
        for i in range(0, X_test.size()[0], batch_size):
        
            # Turn off gradients for validation, saves memory and computations
            with torch.no_grad():
                
                model.eval()
                indices = permutation[i:i+batch_size]
                batch_x, batch_y = X_test[indices], y_test[indices]
                log_ps = model(batch_x)
                test_loss += criterion(log_ps, batch_y)
                
                ps = torch.exp(log_ps)
                top_p, top_class = ps.topk(1, dim=1)
                equals = top_class == batch_y.view(*top_class.shape)
                accuracy += torch.mean(equals.type(torch.FloatTensor))
                      
        model.train()
        
        test_losses.append(test_loss/testLoaderSize)

        print("Test Loss: {:.3f}.. ".format(test_loss/testLoaderSize),
             "Test Accuracy: {:.3f}".format(accuracy/testLoaderSize))
    
        #plt.plot(train_losses)
        #plt.plot(test_losses)

def getLowConfidence(unlabeledData):
    
    confidences = []
        
    # only apply the model to a limited number of items
    indices = torch.randperm(unlabeledData.size()[0])
    #indices = permutation[:limit]
    unlabeledData = unlabeledData[indices]        
         
    with torch.no_grad():
        
        for i, item in enumerate(unlabeledData):
            
            item = item.view(1, -1)
            
            log_probs = model(item)

            # get confidence that it is related
            prob_related = math.exp(log_probs.data.tolist()[0][1]) 
            
            ps = torch.exp(log_probs)
            top_p, top_class = ps.topk(1, dim=1)
            
            if prob_related < 0.5:
                confidence = 1 - prob_related
            else:
                confidence = prob_related 
                
            confidenceObject = []
            confidenceObject.append(indices[i].item())
            confidenceObject.append(confidence)  
            confidenceObject.append(top_class.item())
            confidences.append(confidenceObject)

    confidences.sort(key=lambda x: x[1])
    return confidences

# Load data 
xHarData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/train/X_train.txt', header=None)
yHarData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/train/y_train.txt', header=None)
X_testData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/test/X_test.txt', header=None)
y_testData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/test/y_test.txt', header=None)

# Transform data
trainData =[]
for i in range(len(xHarData[0])):
    trainData.append(transform(xHarData[0][i]))
    
testData = []
for i in range(len(X_testData[0])):
    testData.append((transform(X_testData[0][i])))

# Prepare training and test data
X_train = torch.tensor(trainData)
y_train = torch.tensor(yHarData[0].values).flatten()

X_test = torch.tensor(testData)
y_test = torch.tensor(y_testData[0].values).flatten()

# Train and Evaluate Model
trainAndTestModel(X_train[:7001], y_train[:7001], X_test, y_test)



# Prepare annotated data for Active Learning
lowConfidence = getLowConfidence(X_train[7001:])
tlist = list(zip(*lowConfidence))

unlabeledIndices = torch.tensor(tlist[0])
unlabeledIndices = torch.add(unlabeledIndices, 7001)

predictedValues = torch.tensor(tlist[2])

X_unlabeled = X_train[unlabeledIndices]
y_unlabeled = y_train[unlabeledIndices][:101]

y_unlabeled = torch.cat((y_unlabeled, predictedValues[101:]), 0)


X_unlabeled = torch.cat((X_unlabeled, X_train[:7001]), 0)
y_unlabeled = torch.cat((y_unlabeled, y_train[:7001]), 0)

# Train and Evaluate Model
trainAndTestModel(X_unlabeled, y_unlabeled, X_test, y_test)


