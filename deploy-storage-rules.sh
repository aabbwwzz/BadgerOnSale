#!/bin/bash

echo "🚀 Deploying Firebase Storage Rules..."
echo ""
echo "Step 1: Logging in to Firebase..."
echo "A browser window will open. Please log in with your Google account."
echo ""

firebase login

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Login successful!"
    echo ""
    echo "Step 2: Deploying storage rules..."
    firebase deploy --only storage
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Storage rules deployed successfully!"
        echo "🎉 Your camera feature is now ready to use!"
    else
        echo ""
        echo "❌ Deployment failed. Please check the error messages above."
    fi
else
    echo ""
    echo "❌ Login failed. Please try again."
fi

